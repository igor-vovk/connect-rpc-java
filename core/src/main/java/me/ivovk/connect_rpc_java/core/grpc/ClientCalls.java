package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.*;

import java.util.concurrent.CompletableFuture;

public class ClientCalls {

  public record Response<T>(Metadata headers, T value, Metadata trailers) {}

  public static <Req, Resp> CompletableFuture<Response<Resp>> unaryCall(
      Channel channel,
      MethodDescriptor<Req, Resp> method,
      CallOptions options,
      Metadata headers,
      Req request) {
    var call = channel.newCall(method, options);
    var future = new CompletableFuture<Response<Resp>>();

    var listener =
        new ClientCall.Listener<Resp>() {
          private Metadata headers;
          private Resp message;

          @Override
          public void onHeaders(Metadata headers) {
            this.headers = headers;
          }

          @Override
          public void onMessage(Resp message) {
            if (this.message != null) {
              future.completeExceptionally(
                  new IllegalStateException("More than one message received"));
            }
            this.message = message;
          }

          @Override
          public void onClose(Status status, Metadata trailers) {
            if (status.isOk()) {
              if (message != null) {
                future.complete(new Response<>(headers, message, trailers));
              } else {
                future.completeExceptionally(new IllegalStateException("No value received"));
              }
            } else {
              future.completeExceptionally(status.asException());
            }
          }
        };

    call.start(listener, headers);
    call.sendMessage(request);
    call.halfClose();
    // Request 2 messages to catch a case where a server sends more than one message
    call.request(2);

    return future;
  }
}
