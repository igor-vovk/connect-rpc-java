package me.ivovk.connect_rpc_java.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.netty.connect.ConnectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

  private final MethodRegistry methodRegistry;
  private final ConnectHandler connectHandler;
  private final HeaderMapping<HttpHeaders> headerMapping;

  private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

  public HttpServerHandler(
      MethodRegistry methodRegistry,
      ConnectHandler connectHandler,
      HeaderMapping<HttpHeaders> headerMapping) {
    this.methodRegistry = methodRegistry;
    this.connectHandler = connectHandler;
    this.headerMapping = headerMapping;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest req) {
      if (logger.isTraceEnabled()) {
        logger.trace(">>> HTTP request: {} {}", req.method(), req.uri());
        logger.trace(">>> Headers: {}", req.headers());
      }

      var decodedUri = new QueryStringDecoder(req.uri());

      var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CREATED);
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);

      ctx.writeAndFlush(response);
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
    super.channelReadComplete(ctx);
  }
}
