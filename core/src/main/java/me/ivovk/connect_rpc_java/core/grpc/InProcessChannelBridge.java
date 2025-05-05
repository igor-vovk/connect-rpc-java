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

  record ChannelContext(
      ManagedChannel channel,
      Server server,
      Duration awaitTerminationTimeout
  ) {
    public void shutdown() throws InterruptedException {
      server.shutdown();
      channel.shutdown();

      server.awaitTermination(awaitTerminationTimeout.getSeconds(), TimeUnit.SECONDS);
      channel.awaitTermination(awaitTerminationTimeout.getSeconds(), TimeUnit.SECONDS);
    }
  }

  /**
   * Remember to call shutdown() on the returned ChannelContext
   */
  static ChannelContext create(
      List<ServerServiceDefinition> services,
      Configurer<ServerBuilder<?>> serverBuilderConfigurer,
      Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer,
      Executor executor,
      Duration awaitTerminationTimeout
  ) {
    String name = InProcessServerBuilder.generateName();

    Server server = createServer(name, services, serverBuilderConfigurer, executor);
    ManagedChannel channel = createChannel(name, channelBuilderConfigurer);

    return new ChannelContext(channel, server, awaitTerminationTimeout);
  }

  private static Server createServer(
      String name,
      List<ServerServiceDefinition> services,
      Configurer<ServerBuilder<?>> serverBuilderConfigurer,
      Executor executor
  ) {
    ServerBuilder<?> builder = InProcessServerBuilder.forName(name)
        .addServices(services)
        .executor(executor);

    builder = serverBuilderConfigurer.configure(builder);

    return builder.build();
  }

  private static ManagedChannel createChannel(
      String name,
      Configurer<ManagedChannelBuilder<?>> channelBuilderConfigurer
  ) {
    ManagedChannelBuilder<?> builder = InProcessChannelBuilder.forName(name);

    builder = channelBuilderConfigurer.configure(builder);

    return builder.build();
  }

}
