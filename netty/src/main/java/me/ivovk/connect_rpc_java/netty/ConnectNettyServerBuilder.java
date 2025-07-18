package me.ivovk.connect_rpc_java.netty;

import com.google.protobuf.TypeRegistry;
import io.grpc.BindableService;
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
import me.ivovk.connect_rpc_java.core.http.Paths;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;
import me.ivovk.connect_rpc_java.netty.server.ConnectErrorHandler;
import me.ivovk.connect_rpc_java.netty.server.ConnectHandler;
import me.ivovk.connect_rpc_java.netty.server.HttpServerHandler;
import me.ivovk.connect_rpc_java.netty.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConnectNettyServerBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ConnectNettyServerBuilder.class);

  private static final String DEFAULT_HOST = "0.0.0.0";
  private static final int DEFAULT_PORT = 0;

  private List<ServerServiceDefinition> services;
  private Configurer<ServerBuilder<?>> serverBuilderConfigurer = Configurer.noop();
  private Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer = Configurer.noop();
  private Configurer<TypeRegistry.Builder> jsonTypeRegistryConfigurer = Configurer.noop();
  private Predicate<String> incomingHeadersFilter = HeaderMapping.DEFAULT_INCOMING_HEADERS_FILTER;
  private Predicate<String> outgoingHeadersFilter = HeaderMapping.DEFAULT_OUTGOING_HEADERS_FILTER;
  private Paths.Path pathPrefix = Paths.Path.ROOT_PATH;
  private Executor executor = Executors.newCachedThreadPool();
  private Duration terminationTimeout = Duration.ofSeconds(5);
  private boolean treatTrailersAsHeaders = true;
  private boolean enableLogging = false;
  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;

  private ConnectNettyServerBuilder() {}

  public static ConnectNettyServerBuilder forServices(BindableService... services) {
    var serverServiceDefinitions = Stream.of(services).map(BindableService::bindService).toList();

    return forServices(serverServiceDefinitions);
  }

  public static ConnectNettyServerBuilder forServices(ServerServiceDefinition... services) {
    return forServices(List.of(services));
  }

  private static ConnectNettyServerBuilder forServices(List<ServerServiceDefinition> services) {
    ConnectNettyServerBuilder builder = new ConnectNettyServerBuilder();
    builder.services = services;

    return builder;
  }

  public ConnectNettyServerBuilder serverBuilderConfigurer(
      Configurer<ServerBuilder<?>> serverBuilderConfigurer) {
    this.serverBuilderConfigurer = serverBuilderConfigurer;

    return this;
  }

  public ConnectNettyServerBuilder channelBuilderConfigurer(
      Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer) {
    this.channelBuilderConfigurer = channelBuilderConfigurer;

    return this;
  }

  public ConnectNettyServerBuilder jsonTypeRegistryConfigurer(
      Configurer<TypeRegistry.Builder> jsonTypeRegistryConfigurer) {
    this.jsonTypeRegistryConfigurer = jsonTypeRegistryConfigurer;

    return this;
  }

  public ConnectNettyServerBuilder incomingHeadersFilter(Predicate<String> incomingHeadersFilter) {
    this.incomingHeadersFilter = incomingHeadersFilter;

    return this;
  }

  public ConnectNettyServerBuilder outgoingHeadersFilter(Predicate<String> outgoingHeadersFilter) {
    this.outgoingHeadersFilter = outgoingHeadersFilter;

    return this;
  }

  public ConnectNettyServerBuilder pathPrefix(Paths.Path pathPrefix) {
    this.pathPrefix = pathPrefix;

    return this;
  }

  public ConnectNettyServerBuilder executor(Executor executor) {
    this.executor = executor;

    return this;
  }

  public ConnectNettyServerBuilder terminationTimeout(Duration terminationTimeout) {
    this.terminationTimeout = terminationTimeout;

    return this;
  }

  public ConnectNettyServerBuilder treatTrailersAsHeaders(boolean treatTrailersAsHeaders) {
    this.treatTrailersAsHeaders = treatTrailersAsHeaders;

    return this;
  }

  public ConnectNettyServerBuilder enableLogging(boolean enableLogging) {
    this.enableLogging = enableLogging;

    return this;
  }

  public ConnectNettyServerBuilder host(String host) {
    this.host = host;

    return this;
  }

  public ConnectNettyServerBuilder port(int port) {
    this.port = port;

    return this;
  }

  public NettyServer build() throws InterruptedException, IOException {
    var channelContext =
        InProcessChannelBridge.create(
            services,
            serverBuilderConfigurer,
            channelBuilderConfigurer,
            executor,
            terminationTimeout);

    var jsonTypeRegistry = jsonTypeRegistryConfigurer.configure(TypeRegistry.newBuilder()).build();
    var jsonMarshallerFactory = new JsonMarshallerFactory(jsonTypeRegistry);

    var methodRegistry = MethodRegistry.create(services, jsonMarshallerFactory);

    var headerMapping =
        new NettyHeaderMapping(
            incomingHeadersFilter, outgoingHeadersFilter, treatTrailersAsHeaders);

    var errorHandler = new ConnectErrorHandler(headerMapping);

    var connectHandler = new ConnectHandler(channelContext.channel(), errorHandler, headerMapping);

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
                            new HttpServerHandler(
                                methodRegistry, connectHandler, headerMapping, pathPrefix));
                  }
                });

    var channel = bootstrap.bind(host, port).sync().channel();

    Runnable shutdown =
        () -> {
          try {
            channel.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            channelContext.shutdown();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error shutting down server", e);
          }
        };

    return new NettyServer((InetSocketAddress) channel.localAddress(), shutdown);
  }
}
