package me.ivovk.connect_rpc_java.netty;

import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import me.ivovk.connect_rpc_java.core.Configurer;
import me.ivovk.connect_rpc_java.core.grpc.InProcessChannelBridge;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.netty.connect.ConnectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class NettyServerBuilder {

  private static final Logger logger = LoggerFactory.getLogger(NettyServerBuilder.class);

  private static final String DEFAULT_HOST = "0.0.0.0";
  private static final int DEFAULT_PORT = 0;

  private List<ServerServiceDefinition> services;
  private Configurer<ServerBuilder<?>> serverBuilderConfigurer = Configurer.noop();
  private Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer = Configurer.noop();
  private Predicate<String> incomingHeadersFilter = HeaderMapping.DEFAULT_INCOMING_HEADERS_FILTER;
  private Predicate<String> outgoingHeadersFilter = HeaderMapping.DEFAULT_OUTGOING_HEADERS_FILTER;
  private Executor executor = Executors.newCachedThreadPool();
  private Duration terminationTimeout = Duration.ofSeconds(5);
  private boolean treatTrailersAsHeaders = true;
  private boolean enableLogging = false;
  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;

  private NettyServerBuilder() {}

  public static NettyServerBuilder forService(ServerServiceDefinition service) {
    return forServices(List.of(service));
  }

  public static NettyServerBuilder forServices(List<ServerServiceDefinition> services) {
    NettyServerBuilder builder = new NettyServerBuilder();
    builder.services = services;

    return builder;
  }

  public NettyServerBuilder setServerBuilderConfigurer(
      Configurer<ServerBuilder<?>> serverBuilderConfigurer) {
    this.serverBuilderConfigurer = serverBuilderConfigurer;

    return this;
  }

  public NettyServerBuilder setChannelBuilderConfigurer(
      Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer) {
    this.channelBuilderConfigurer = channelBuilderConfigurer;

    return this;
  }

  public NettyServerBuilder setIncomingHeadersFilter(Predicate<String> incomingHeadersFilter) {
    this.incomingHeadersFilter = incomingHeadersFilter;

    return this;
  }

  public NettyServerBuilder setOutgoingHeadersFilter(Predicate<String> outgoingHeadersFilter) {
    this.outgoingHeadersFilter = outgoingHeadersFilter;

    return this;
  }

  public NettyServerBuilder setExecutor(Executor executor) {
    this.executor = executor;

    return this;
  }

  public NettyServerBuilder setTerminationTimeout(Duration terminationTimeout) {
    this.terminationTimeout = terminationTimeout;

    return this;
  }

  public NettyServerBuilder setTreatTrailersAsHeaders(boolean treatTrailersAsHeaders) {
    this.treatTrailersAsHeaders = treatTrailersAsHeaders;

    return this;
  }

  public NettyServerBuilder setEnableLogging(boolean enableLogging) {
    this.enableLogging = enableLogging;

    return this;
  }

  public NettyServerBuilder setHost(String host) {
    this.host = host;

    return this;
  }

  public NettyServerBuilder setPort(int port) {
    this.port = port;

    return this;
  }

  public NettyServer build() throws InterruptedException {
    var channelContext =
        InProcessChannelBridge.create(
            services,
            serverBuilderConfigurer,
            channelBuilderConfigurer,
            executor,
            terminationTimeout);

    var methodRegistry = MethodRegistry.create(services);

    var headerMapping =
        new NettyHeaderMapping(
            incomingHeadersFilter, outgoingHeadersFilter, treatTrailersAsHeaders);

    var connectHandler = new ConnectHandler(channelContext.channel(), headerMapping);

    var ioHandlerFactory = NioIoHandler.newFactory();
    var bossGroup = new MultiThreadIoEventLoopGroup(1, ioHandlerFactory);
    var workerGroup = new MultiThreadIoEventLoopGroup(1, ioHandlerFactory);

    var bootstrap =
        new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(
                new ChannelInitializer<SocketChannel>() {
                  @Override
                  protected void initChannel(SocketChannel ch) {
                    var pipeline = ch.pipeline();

                    if (enableLogging) {
                      pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));
                    }

                    pipeline
                        .addLast("serverCodec", new HttpServerCodec())
                        .addLast("keepAlive", new HttpServerKeepAliveHandler())
                        .addLast("aggregator", new HttpObjectAggregator(1024 * 1024))
                        .addLast("idleStateHandler", new IdleStateHandler(60, 30, 0))
                        .addLast("readTimeoutHandler", new ReadTimeoutHandler(30))
                        .addLast("writeTimeoutHandler", new WriteTimeoutHandler(30))
                        .addLast(
                            "handler",
                            new HttpServerHandler(methodRegistry, connectHandler, headerMapping));
                  }
                });

    var channel = bootstrap.bind(host, port).sync().channel();

    Runnable shutdown =
        () -> {
          try {
            channel.close().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            channelContext.shutdown();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error shutting down server", e);
          }
        };

    return new NettyServer((InetSocketAddress) channel.localAddress(), shutdown);
  }
}
