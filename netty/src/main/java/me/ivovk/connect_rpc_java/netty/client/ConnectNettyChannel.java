package me.ivovk.connect_rpc_java.netty.client;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.handler.codec.http.HttpHeaders;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectNettyChannel extends ManagedChannel {

  private static final IoHandlerFactory ioHandlerFactory = NioIoHandler.newFactory();
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final ClientCallParams params;

  public ConnectNettyChannel(
      String host,
      int port,
      int timeout,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory jsonMarshallerFactory) {
    var workerGroup = new MultiThreadIoEventLoopGroup(1, ioHandlerFactory);

    this.params =
        new ClientCallParams(
            timeout, workerGroup, host, port, headerMapping, jsonMarshallerFactory);
  }

  @Override
  public <Req, Resp> ClientCall<Req, Resp> newCall(
      MethodDescriptor<Req, Resp> md, CallOptions callOptions) {
    if (shutdown.get()) {
      return new ShutdownClientCall<>();
    }

    @SuppressWarnings("unchecked")
    MethodDescriptor<Req, ? extends Message> methodDescriptor =
        (MethodDescriptor<Req, ? extends Message>) md;

    @SuppressWarnings("unchecked")
    ClientCall<Req, Resp> clientCall =
        (ClientCall<Req, Resp>)
            new ClientCallImpl<>(
                params, methodDescriptor, callOptions, getCallExecutor(callOptions));

    return clientCall;
  }

  @Override
  public String authority() {
    return params.hostname;
  }

  Executor getCallExecutor(CallOptions callOptions) {
    Executor executor = callOptions.getExecutor();
    if (executor == null) {
      return null;
    }
    return executor;
  }

  @Override
  public ManagedChannel shutdown() {
    if (shutdown.compareAndSet(false, true)) {
      params.workerGroup.shutdownGracefully();
    }
    return this;
  }

  @Override
  public boolean isShutdown() {
    return shutdown.get();
  }

  @Override
  public boolean isTerminated() {
    return params.workerGroup.isTerminated();
  }

  @Override
  public ManagedChannel shutdownNow() {
    shutdown();
    return this;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (shutdown.compareAndSet(false, true)) {
      params.workerGroup.shutdownGracefully(0, timeout, unit);
    }

    return isTerminated();
  }
}
