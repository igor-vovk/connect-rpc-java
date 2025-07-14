package me.ivovk.connect_rpc_java.netty.client;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.netty.channel.EventLoopGroup;
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
  private final String host;
  private final int port;
  private final int timeout;
  private final HeaderMapping<HttpHeaders> headerMapping;
  private final JsonMarshallerFactory jsonMarshallerFactory;
  private final EventLoopGroup workerGroup;
  private final AtomicBoolean shutdown = new AtomicBoolean(false);

  public ConnectNettyChannel(
      String host,
      int port,
      int timeout,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory jsonMarshallerFactory) {
    this.host = host;
    this.port = port;
    this.timeout = timeout;
    this.headerMapping = headerMapping;
    this.jsonMarshallerFactory = jsonMarshallerFactory;
    this.workerGroup = new MultiThreadIoEventLoopGroup(1, ioHandlerFactory);
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
                methodDescriptor,
                callOptions,
                timeout,
                workerGroup,
                host,
                port,
                headerMapping,
                jsonMarshallerFactory,
                getCallExecutor(callOptions));

    return clientCall;
  }

  @Override
  public String authority() {
    return host + ":" + port;
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
      workerGroup.shutdownGracefully();
    }
    return this;
  }

  @Override
  public boolean isShutdown() {
    return shutdown.get();
  }

  @Override
  public boolean isTerminated() {
    return workerGroup.isTerminated();
  }

  @Override
  public ManagedChannel shutdownNow() {
    shutdown();
    return this;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    if (shutdown.compareAndSet(false, true)) {
      workerGroup.shutdownGracefully(0, timeout, unit);
    }

    return isTerminated();
  }
}
