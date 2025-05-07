package me.ivovk.connect_rpc_java.netty;

import java.net.InetSocketAddress;

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
