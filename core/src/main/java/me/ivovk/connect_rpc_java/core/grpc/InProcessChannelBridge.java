package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import me.ivovk.connect_rpc_java.core.Configurer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class InProcessChannelBridge {

  public record ChannelContext(ManagedChannel channel, Server server, Duration terminationTimeout) {
    public void shutdown() throws InterruptedException {
      server.shutdown();
      channel.shutdown();

      server.awaitTermination(terminationTimeout.getSeconds(), TimeUnit.SECONDS);
      channel.awaitTermination(terminationTimeout.getSeconds(), TimeUnit.SECONDS);
    }

    public boolean isShutdown() {
      return server.isShutdown() && channel.isShutdown();
    }

    public boolean isTerminated() {
      return server.isTerminated() && channel.isTerminated();
    }
  }

  /** Remember to call shutdown() on the returned ChannelContext */
  public static ChannelContext create(
      List<ServerServiceDefinition> services,
      Configurer<ServerBuilder<?>> serverBuilderConfigurer,
      Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer,
      Executor executor,
      Duration awaitTerminationTimeout) {
    var name = InProcessServerBuilder.generateName();

    var server = createServer(name, services, serverBuilderConfigurer, executor);
    var channel = createChannel(name, channelBuilderConfigurer);

    return new ChannelContext(channel, server, awaitTerminationTimeout);
  }

  static Server createServer(
      String name,
      List<ServerServiceDefinition> services,
      Configurer<ServerBuilder<?>> serverBuilderConfigurer,
      Executor executor) {
    ServerBuilder<?> builder =
        InProcessServerBuilder.forName(name).addServices(services).executor(executor);

    return serverBuilderConfigurer.configure(builder).build();
  }

  static ManagedChannel createChannel(
      String name, Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer) {
    ManagedChannelBuilder<?> builder = InProcessChannelBuilder.forName(name);

    return channelBuilderConfigurer.configure(builder).build();
  }
}
