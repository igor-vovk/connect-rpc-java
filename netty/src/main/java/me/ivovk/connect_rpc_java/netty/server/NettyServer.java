package me.ivovk.connect_rpc_java.netty.server;

import java.net.InetSocketAddress;

/**
 * This class is used to obtain information about the running server and to perform graceful shutdown.
 */
public class NettyServer implements AutoCloseable {
  private final InetSocketAddress address;
  private final Runnable onShutdown;

  public NettyServer(InetSocketAddress address, Runnable onShutdown) {
    this.address = address;
    this.onShutdown = onShutdown;
  }

  public String getHost() {
    return address.getHostName();
  }

  public int getPort() {
    return address.getPort();
  }

  public void shutdown() {
    onShutdown.run();
  }

  @Override
  public void close() {
    onShutdown.run();
  }
}
