package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.*;

import java.util.concurrent.CompletableFuture;

public class ClientCalls {

  public record Response<T>(T message, Metadata headers, Metadata trailers) {}

  public static <ReqT, RespT> CompletableFuture<Response<RespT>> completableFutureUnaryCall(
      ClientCall<ReqT, RespT> call, Metadata headers, ReqT request) {
    var listener = new CompletableFutureListener<RespT>();

    call.start(listener, headers);
    call.sendMessage(request);

    call.halfClose();

    // request 2 messages to catch a case when a server sends more than one message
    call.request(2);

    return listener.getResponse();
  }

  private static class CompletableFutureListener<RespT> extends ClientCall.Listener<RespT> {

    private final CompletableFuture<Response<RespT>> responseFuture = new CompletableFuture<>();

    private Metadata headers = null;
    private RespT message = null;

    @Override
    public void onHeaders(Metadata headers) {
      this.headers = headers;
    }

    @Override
    public void onMessage(RespT message) {
      if (this.message != null) {
        throw new IllegalStateException("More than one message received");
      }

      this.message = message;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
      if (status.isOk()) {
        responseFuture.complete(new Response<>(message, headers, trailers));
      } else {
        responseFuture.completeExceptionally(status.asRuntimeException(trailers));
      }
    }

    public CompletableFuture<Response<RespT>> getResponse() {
      return responseFuture;
    }
  }
}
