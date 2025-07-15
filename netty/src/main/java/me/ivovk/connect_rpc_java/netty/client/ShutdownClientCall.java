package me.ivovk.connect_rpc_java.netty.client;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;

import javax.annotation.Nullable;

class ShutdownClientCall<Resp, Req> extends ClientCall<Req, Resp> {
  private static final Status status =
      Status.UNAVAILABLE.withDescription("Channel shutdown invoked");

  @Override
  public void start(Listener<Resp> responseListener, Metadata headers) {
    responseListener.onClose(status, new Metadata());
  }

  @Override
  public void request(int numMessages) {}

  @Override
  public void cancel(@Nullable String message, @Nullable Throwable cause) {}

  @Override
  public void halfClose() {}

  @Override
  public void sendMessage(Req message) {}
}
