package me.ivovk.connect_rpc_java.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

  private final MethodRegistry methodRegistry;
  private final HeaderMapping<HttpHeaders> headerMapping;

  private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

  public HttpServerHandler(
      MethodRegistry methodRegistry,
      HeaderMapping<HttpHeaders> headerMapping
  ) {
    this.methodRegistry = methodRegistry;
    this.headerMapping = headerMapping;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest req) {
      if (logger.isTraceEnabled()) {
        logger.trace(">>> HTTP request: {} {}", req.method(), req.uri());
        logger.trace(">>> Headers: {}", req.headers());
      }

      ctx.writeAndFlush("200 OK");
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
    super.channelReadComplete(ctx);
  }
}
