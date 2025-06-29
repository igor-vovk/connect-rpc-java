package me.ivovk.connect_rpc_java.netty;

import com.google.protobuf.TypeRegistry;
import io.grpc.Channel;
import me.ivovk.connect_rpc_java.core.Configurer;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;
import me.ivovk.connect_rpc_java.netty.client.ConnectNettyChannel;

public class ConnectNettyChannelBuilder {

  private String host;
  private int port;
  private Configurer<TypeRegistry.Builder> jsonTypeRegistryConfigurer = Configurer.noop();

  private ConnectNettyChannelBuilder() {}

  public static ConnectNettyChannelBuilder forAddress(String host, int port) {
    ConnectNettyChannelBuilder builder = new ConnectNettyChannelBuilder();
    builder.host = host;
    builder.port = port;

    return builder;
  }

  public ConnectNettyChannelBuilder jsonTypeRegistryConfigurer(
      Configurer<TypeRegistry.Builder> jsonTypeRegistryConfigurer) {
    this.jsonTypeRegistryConfigurer = jsonTypeRegistryConfigurer;

    return this;
  }

  public Channel build() {
    var headerMapping =
        new NettyHeaderMapping(
            h -> !h.equalsIgnoreCase("Connection"), h -> !h.equalsIgnoreCase("Connection"), true);

    var jsonTypeRegistry = jsonTypeRegistryConfigurer.configure(TypeRegistry.newBuilder()).build();
    var jsonMarshallerFactory = new JsonMarshallerFactory(jsonTypeRegistry);

    return new ConnectNettyChannel(host, port, headerMapping, jsonMarshallerFactory);
  }
}
