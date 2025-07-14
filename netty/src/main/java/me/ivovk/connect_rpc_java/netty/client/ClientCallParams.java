package me.ivovk.connect_rpc_java.netty.client;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpHeaders;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;

public class ClientCallParams {
  public final int timeout;
  public final EventLoopGroup workerGroup;
  public final String host;
  public final int port;
  public final String hostname;
  public final HeaderMapping<HttpHeaders> headerMapping;
  public final JsonMarshallerFactory jsonMarshallerFactory;

  public ClientCallParams(
      int timeout,
      EventLoopGroup workerGroup,
      String host,
      int port,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory jsonMarshallerFactory) {
    this.timeout = timeout;
    this.workerGroup = workerGroup;
    this.host = host;
    this.port = port;
    this.hostname = host + ":" + port;
    this.headerMapping = headerMapping;
    this.jsonMarshallerFactory = jsonMarshallerFactory;
  }
}
