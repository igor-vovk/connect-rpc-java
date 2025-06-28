package me.ivovk.connect_rpc_java.netty;

import io.grpc.Channel;
import me.ivovk.connect_rpc_java.netty.client.ConnectNettyChannel;

public class ConnectNettyChannelBuilder {

  private String host;
  private int port;

  private ConnectNettyChannelBuilder() {}

  public static ConnectNettyChannelBuilder forAddress(String host, int port) {
    ConnectNettyChannelBuilder builder = new ConnectNettyChannelBuilder();
    builder.host = host;
    builder.port = port;

    return builder;
  }

  public Channel build() {
    var headerMapping = new NettyHeaderMapping(h -> true, h -> true, true);

    return new ConnectNettyChannel(host, port, headerMapping);
  }
}
