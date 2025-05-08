package me.ivovk.connect_rpc_java.netty.connect;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Status;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import me.ivovk.connect_rpc_java.core.grpc.ClientCalls;
import me.ivovk.connect_rpc_java.core.grpc.GrpcHeaders;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.netty.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public CompletableFuture<HttpResponse> handle(Request request, MethodRegistry.Entry method) {
    switch (method.descriptor().getType()) {
      case UNARY:
        return handleUnary(request, method);
      default:
        logger.warn("Unsupported method type: {}", method.descriptor().getType());

        throw Status.UNIMPLEMENTED
            .withDescription("Unsupported method type: " + method.descriptor().getType())
            .asRuntimeException();
    }
  }

  private CompletableFuture<HttpResponse> handleUnary(
      Request request, MethodRegistry.Entry method) {
    if (logger.isTraceEnabled()) {
      // Used in conformance tests
      Optional.ofNullable(request.headers().get((GrpcHeaders.X_TEST_CASE_NAME)))
          .ifPresent(caseName -> logger.trace(">>> Test Case name: {}", caseName));
    }

    GeneratedMessageV3 requestMessage = null;
    var call = channel.newCall(method.descriptor(), CallOptions.DEFAULT);
    var response = ClientCalls.completableFutureUnaryCall(call, request.headers(), requestMessage);

    return null;
  }
}
