package me.ivovk.connect_rpc_java.conformance;

import static me.ivovk.connect_rpc_java.conformance.util.ConformanceHeadersConv.toHeaderList;

import com.google.protobuf.Any;
import connectrpc.conformance.v1.*;
import connectrpc.conformance.v1.Error;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import me.ivovk.connect_rpc_java.conformance.util.ConformanceHeadersConv;
import me.ivovk.connect_rpc_java.conformance.util.LengthPrefixedProtoSerde;
import me.ivovk.connect_rpc_java.core.connect.ErrorHandling;
import me.ivovk.connect_rpc_java.core.grpc.ClientCalls;
import me.ivovk.connect_rpc_java.netty.ConnectNettyChannelBuilder;
import me.ivovk.connect_rpc_java.netty.client.ConnectNettyChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NettyClientLauncher {

  private static final Logger logger = LoggerFactory.getLogger(NettyClientLauncher.class);

  public static void main(String[] args) throws Exception {
    logger.info("Starting conformance client tests...");

    var serde = LengthPrefixedProtoSerde.forSystemInOut();

    ClientCompatRequest request;
    while ((request = serde.read(ClientCompatRequest.parser())) != null) {
      try {
        ClientCompatResponse response = runTestCase(request);

        logger.info("<<< Writing response to test runner: {}", response);

        serde.write(response);
      } catch (Throwable t) {
        logger.error("Error running test case", t);

        var clientError =
            ClientErrorResult.newBuilder()
                .setMessage("Error running conformance test: " + t.getMessage())
                .build();

        serde.write(
            ClientCompatResponse.newBuilder()
                .setTestName(request.getTestName())
                .setError(clientError)
                .build());
      }
    }
  }

  private static ClientCompatResponse runTestCase(ClientCompatRequest spec) {
    var log = String.format(">>> Running conformance test: %s", spec.getTestName());
    logger.info("–".repeat(log.length()));
    logger.info(log);

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
                    b.add(UnaryRequest.getDescriptor()).add(IdempotentUnaryRequest.getDescriptor()))
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
      var metadata = ConformanceHeadersConv.toMetadata(spec.getRequestHeadersList());
      var request = md.parseRequest(spec.getRequestMessages(0).getValue().newInput());

      logger.info(">>> Request metadata: {}", metadata);
      logger.info(">>> Request body: {}", request);

      var callOptions = CallOptions.DEFAULT;
      if (spec.hasTimeoutMs()) {
        callOptions = callOptions.withDeadlineAfter(spec.getTimeoutMs(), TimeUnit.MILLISECONDS);
      }

      var callResult = ClientCalls.unaryCall2(channel, md, callOptions, metadata, request);

      if (spec.getCancel().getAfterCloseSendMs() > 0) {
        new Thread(
            () -> {
              try {
                Thread.sleep(spec.getCancel().getAfterCloseSendMs());
                logger.info(">>> Cancelling conformance test: {}", spec.getTestName());

                callResult.call().cancel("Requested by specification", null);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            });
      }

      var response = callResult.future().join();

      logger.info("<<< Conformance test completed: {}", spec.getTestName());

      return ClientCompatResponse.newBuilder()
          .setTestName(spec.getTestName())
          .setResponse(
              ClientResponseResult.newBuilder()
                  .addAllResponseHeaders(toHeaderList(response.headers()))
                  .addAllPayloads(extractPayloads.apply(response.value()))
                  .addAllResponseTrailers(toHeaderList(response.trailers()))
                  .build())
          .build();
    } catch (Throwable t) {
      logger.error("Error during conformance test: {}", spec.getTestName(), t);
      var errorDetails = ErrorHandling.extractDetails(t);
      logger.info("Determined error details: {}", errorDetails);

      var responseResult =
          ClientResponseResult.newBuilder()
              .addAllResponseHeaders(toHeaderList(errorDetails.headers()))
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
              .addAllResponseTrailers(toHeaderList(errorDetails.trailers()))
              .build();

      return ClientCompatResponse.newBuilder()
          .setTestName(spec.getTestName())
          .setResponse(responseResult)
          .build();
    }
  }
}
