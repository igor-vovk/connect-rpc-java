package me.ivovk.connect_rpc_java.core.grpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import me.ivovk.connect_rpc_java.core.Configurer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Executors;

class InProcessChannelBridgeTest {

  @Test
  void testChannelContextShutdown() throws Exception {
    // Act
    InProcessChannelBridge.ChannelContext context =
        InProcessChannelBridge.create(
            Collections.emptyList(),
            Configurer.noop(),
            Configurer.noop(),
            Executors.newSingleThreadExecutor(),
            Duration.ofSeconds(5));

    // Assert: Server and channel are running
    Server server = context.server();
    ManagedChannel channel = context.channel();

    assertFalse(context.isShutdown(), "Context should be running initially");
    assertFalse(context.isTerminated(), "Server should be running initially");

    // Act: Shutdown
    context.shutdown();

    // Assert: Both server and channel are terminated
    assertTrue(server.isShutdown(), "Server should be shutdown");
    assertTrue(channel.isShutdown(), "Channel should be shutdown");
    assertTrue(context.isShutdown(), "Context should be shutdown");

    // Assert: Both server and channel are terminated
    assertTrue(server.isTerminated(), "Server should be terminated");
    assertTrue(channel.isTerminated(), "Channel should be terminated");
    assertTrue(context.isTerminated(), "Context should be terminated");
  }
}
