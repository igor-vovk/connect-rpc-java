package me.ivovk.connect_rpc_java.conformance;

import com.google.protobuf.Any;
import connectrpc.conformance.v1.*;
import connectrpc.conformance.v1.Error;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import me.ivovk.connect_rpc_java.conformance.util.ConformanceHeadersConv;
import me.ivovk.connect_rpc_java.conformance.util.LengthPrefixedProtoSerde;
import me.ivovk.connect_rpc_java.core.connect.ErrorHandling;
import me.ivovk.connect_rpc_java.core.grpc.ClientCalls;
import me.ivovk.connect_rpc_java.netty.ConnectNettyChannelBuilder;
import me.ivovk.connect_rpc_java.netty.client.ConnectNettyChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NettyClientLauncher {

  private static final Logger logger = LoggerFactory.getLogger(NettyClientLauncher.class);

  public static void main(String[] args) {
    logger.info("Starting conformance client tests...");

    var serde = LengthPrefixedProtoSerde.forSystemInOut();

    ClientCompatRequest request;
    while (true) {
      try {
        request = serde.read(ClientCompatRequest.parser());
      } catch (IOException e) {
        break;
      }

      try {
        ClientCompatResponse response = runTestCase(request);

        serde.write(response);
      } catch (Throwable t) {
        logger.error("Error running test case", t);
        try {
          serde.write(
              ClientCompatResponse.newBuilder()
                  .setTestName(request.getTestName())
                  .setError(ClientErrorResult.newBuilder().setMessage(t.getMessage()).build())
                  .build());
        } catch (IOException e) {
          logger.error("Error writing error response", e);
        }
      }
    }
  }

  private static ClientCompatResponse runTestCase(ClientCompatRequest spec) {
    logger.info(">>> Running conformance test: {}", spec.getTestName());

    if (!spec.getService().equals("connectrpc.conformance.v1.ConformanceService")) {
      return ClientCompatResponse.newBuilder()
          .setTestName(spec.getTestName())
          .setError(ClientErrorResult.newBuilder().setMessage("Invalid service name").build())
          .build();
    }

    Channel channel =
        ConnectNettyChannelBuilder.forAddress(spec.getHost(), spec.getPort())
            // Registering message types in TypeRegistry is required to pass
            // com.google.protobuf.any.Any
            // JSON-serialization conformance tests
            .jsonTypeRegistryConfigurer(
                b ->
                    b.add(
                        List.of(
                            UnaryRequest.getDescriptor(), IdempotentUnaryRequest.getDescriptor())))
            .build();

    try {
      return switch (spec.getMethod()) {
        case "Unary" ->
            executeUnaryCall(
                channel,
                spec,
                ConformanceServiceGrpc.getUnaryMethod(),
                r -> List.of(r.getPayload()));
        case "Unimplemented" ->
            executeUnaryCall(
                channel, spec, ConformanceServiceGrpc.getUnimplementedMethod(), r -> List.of());
        default ->
            ClientCompatResponse.newBuilder()
                .setTestName(spec.getTestName())
                .setError(
                    ClientErrorResult.newBuilder()
                        .setMessage("Unsupported method: " + spec.getMethod())
                        .build())
                .build();
      };
    } finally {
      if (channel instanceof ConnectNettyChannel) {
        ((ConnectNettyChannel) channel).shutdown();
      }
    }
  }

  private static <Req, Resp> ClientCompatResponse executeUnaryCall(
      Channel channel,
      ClientCompatRequest spec,
      MethodDescriptor<Req, Resp> md,
      Function<Resp, List<ConformancePayload>> extractPayloads) {
    try {
      Req request = md.parseRequest(spec.getRequestMessages(0).getValue().newInput());
      Metadata metadata = ConformanceHeadersConv.toMetadata(spec.getRequestHeadersList());

      logger.info(">>> Decoded request: {}", request);
      logger.info(">>> Decoded metadata: {}", metadata);

      CallOptions callOptions = CallOptions.DEFAULT;
      if (spec.hasTimeoutMs()) {
        callOptions = callOptions.withDeadlineAfter(spec.getTimeoutMs(), TimeUnit.MILLISECONDS);
      }

      CompletableFuture<ClientCalls.Response<Resp>> responseFuture =
          ClientCalls.unaryCall(channel, md, callOptions, metadata, request);

      if (spec.getCancel().getAfterCloseSendMs() > 0) {
        // TODO: Implement cancellation after a delay
      }

      ClientCalls.Response<Resp> response = responseFuture.join();

      logger.info("<<< Conformance test completed: {}", spec.getTestName());

      return ClientCompatResponse.newBuilder()
          .setTestName(spec.getTestName())
          .setResponse(
              ClientResponseResult.newBuilder()
                  .addAllResponseHeaders(ConformanceHeadersConv.toHeaderList(response.headers()))
                  .addAllPayloads(extractPayloads.apply(response.value()))
                  .addAllResponseTrailers(ConformanceHeadersConv.toHeaderList(response.trailers()))
                  .build())
          .build();
    } catch (Throwable t) {
      logger.error("Error during conformance test: {}", spec.getTestName(), t);
      ErrorHandling.ErrorDetails errorDetails = ErrorHandling.extractDetails(t);

      var responseResult =
          ClientResponseResult.newBuilder()
              .addAllResponseHeaders(ConformanceHeadersConv.toHeaderList(errorDetails.headers()))
              .setError(
                  Error.newBuilder()
                      .setCode(
                          connectrpc.conformance.v1.Code.forNumber(
                              errorDetails.error().getCode().getNumber()))
                      .setMessage(errorDetails.error().getMessage())
                      .addAllDetails(
                          errorDetails.error().getDetailsList().stream()
                              .map(
                                  d ->
                                      Any.newBuilder()
                                          .setTypeUrl("type.googleapis.com/" + d.getType())
                                          .setValue(d.getValue())
                                          .build())
                              .collect(Collectors.toList()))
                      .build())
              .addAllResponseTrailers(ConformanceHeadersConv.toHeaderList(errorDetails.trailers()))
              .build();

      return ClientCompatResponse.newBuilder()
          .setTestName(spec.getTestName())
          .setResponse(responseResult)
          .build();
    }
  }
}
