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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConnectHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Channel channel;
  private final ConnectErrorHandler errorHandler;
  private final HeaderMapping<HttpHeaders> headerMapping;

  public ConnectHandler(
      Channel channel, ConnectErrorHandler errorHandler, HeaderMapping<HttpHeaders> headerMapping) {
    this.channel = channel;
    this.errorHandler = errorHandler;
    this.headerMapping = headerMapping;
  }

  public CompletableFuture<HttpResponse> handle(
      RequestEntity request, MethodRegistry.Entry method) {
    try {
      if (method.methodType() == MethodDescriptor.MethodType.UNARY) {
        return handleUnary(request, method)
            .exceptionally(e -> errorHandler.handle(e, request.mediaType()));
      } else {
        logger.warn("Unsupported method type: {}", method.methodType());

        throw Status.UNIMPLEMENTED
            .withDescription("Unsupported method type: " + method.methodType())
            .asRuntimeException();
      }
    } catch (Exception e) {
      return CompletableFuture.completedFuture(errorHandler.handle(e, request.mediaType()));
    }
  }

  private CompletableFuture<HttpResponse> handleUnary(
      RequestEntity request, MethodRegistry.Entry method) {
    if (logger.isTraceEnabled()) {
      // Used in conformance tests
      Optional.ofNullable(request.headerMetadata().get((GrpcHeaders.X_TEST_CASE_NAME)))
          .ifPresent(caseName -> logger.trace(">>> Test Case name: {}", caseName));
    }

    var callOptions = CallOptions.DEFAULT;
    var timeout = Optional.ofNullable(request.headerMetadata().get(GrpcHeaders.CONNECT_TIMEOUT_MS));
    if (timeout.isPresent()) {
      callOptions = callOptions.withDeadlineAfter(timeout.get(), TimeUnit.MILLISECONDS);
    }

    var call = channel.newCall(method.descriptor(), callOptions);

    return ClientCalls.unaryCall(
            channel, method.descriptor(), callOptions, request.headerMetadata(), request.message())
        .thenApply(
            response -> {
              var httpHeaders =
                  headerMapping
                      .toHeaders(response.headers())
                      .add(headerMapping.trailersToHeaders(response.trailers()))
                      .add(HttpHeaderNames.CONTENT_TYPE, request.mediaType().toString());

              return Response.create(
                  response.value(), method.responseMarshaller(request.mediaType()), httpHeaders);
            });
  }
}
