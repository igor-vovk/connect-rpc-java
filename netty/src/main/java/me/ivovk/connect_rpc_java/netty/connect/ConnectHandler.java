package me.ivovk.connect_rpc_java.netty.connect;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import me.ivovk.connect_rpc_java.core.grpc.ClientCalls;
import me.ivovk.connect_rpc_java.core.grpc.GrpcHeaders;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.netty.RequestEntity;
import me.ivovk.connect_rpc_java.netty.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ConnectHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Channel channel;
  private final HeaderMapping<HttpHeaders> headerMapping;

  public ConnectHandler(Channel channel, HeaderMapping<HttpHeaders> headerMapping) {
    this.channel = channel;
    this.headerMapping = headerMapping;
  }

  public CompletableFuture<HttpResponse> handle(
      RequestEntity request, MethodRegistry.Entry method) {
    if (method.descriptor().getType() == MethodDescriptor.MethodType.UNARY) {
      return handleUnary(request, method);
    } else {
      logger.warn("Unsupported method type: {}", method.descriptor().getType());

      throw Status.UNIMPLEMENTED
          .withDescription("Unsupported method type: " + method.descriptor().getType())
          .asRuntimeException();
    }
  }

  private CompletableFuture<HttpResponse> handleUnary(
      RequestEntity request, MethodRegistry.Entry method) {
    if (logger.isTraceEnabled()) {
      // Used in conformance tests
      Optional.ofNullable(request.headerMetadata().get((GrpcHeaders.X_TEST_CASE_NAME)))
          .ifPresent(caseName -> logger.trace(">>> Test Case name: {}", caseName));
    }

    var call = channel.newCall(method.descriptor(), CallOptions.DEFAULT);

    return ClientCalls.asyncUnaryCall(call, request.headerMetadata(), request.message())
        .thenApply(
            response -> {
              var httpHeaders =
                  headerMapping
                      .toHeaders(response.headerMetadata())
                      .add(headerMapping.trailersToHeaders(response.trailerMetadata()))
                      .add(HttpHeaderNames.CONTENT_TYPE, request.mediaType().toString());

              return Response.create(
                  response.message(),
                  method.responseMarshaller(request.mediaType()),
                  httpHeaders);
            });
  }
}
