package me.ivovk.connect_rpc_java.netty;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.MediaTypes;
import me.ivovk.connect_rpc_java.core.http.Paths;
import me.ivovk.connect_rpc_java.netty.connect.ConnectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

  private final MethodRegistry methodRegistry;
  private final ConnectHandler connectHandler;
  private final HeaderMapping<HttpHeaders> headerMapping;
  private final Paths.Path pathPrefix;

  private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

  public HttpServerHandler(
      MethodRegistry methodRegistry,
      ConnectHandler connectHandler,
      HeaderMapping<HttpHeaders> headerMapping,
      Paths.Path pathPrefix) {
    this.methodRegistry = methodRegistry;
    this.connectHandler = connectHandler;
    this.headerMapping = headerMapping;
    this.pathPrefix = pathPrefix;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest req) {
      if (logger.isTraceEnabled()) {
        logger.trace(">>> HTTP request: {} {}", req.method(), req.uri());
        logger.trace(">>> Headers: {}", req.headers());
      }

      var decodedUri = QueryStringDecoder.builder().build(req.uri());
      var maybePath = Paths.extractPathSegments(decodedUri.rawPath(), pathPrefix);

      var maybeGrpcMethod =
          maybePath
              .filter(path -> path.length() == 2)
              .flatMap(
                  path -> {
                    var serviceName = path.segments()[0];
                    var methodName = path.segments()[1];

                    return methodRegistry.get(serviceName, methodName);
                  });

      if (maybeGrpcMethod.isEmpty()) {
        sendError(ctx, "Method not found", HttpResponseStatus.NOT_FOUND);
        return;
      }

      var grpcMethod = maybeGrpcMethod.get();

      var isGetMethod = req.method() == HttpMethod.GET;

      MediaTypes.MediaType mediaType;
      try {
        var maybeMediaType =
            isGetMethod
                ? queryParam(decodedUri, "encoding").map(MediaTypes::parseShort)
                : Optional.ofNullable(req.headers().get(HttpHeaderNames.CONTENT_TYPE))
                    .map(MediaTypes::parse);

        if (maybeMediaType.isPresent()) {
          mediaType = maybeMediaType.get();
        } else {
          sendError(ctx, "Encoding is missing", HttpResponseStatus.BAD_REQUEST);
          return;
        }
      } catch (IllegalArgumentException e) {
        sendError(ctx, e.getMessage(), HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
        return;
      }

      var requestMarshaller = grpcMethod.requestMarshaller(mediaType);

      var requestMessage = requestMarshaller.parse(new ByteBufInputStream(req.content()));

      var responseFuture =
          connectHandler.handle(
              new RequestEntity(headerMapping.toMetadata(req.headers()), mediaType, requestMessage),
              grpcMethod);

      responseFuture.whenComplete(
          (response, error) -> {
            if (error != null) {
              sendError(ctx, error.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            } else {
              ctx.writeAndFlush(response);
            }
          });
    }
  }

  private void sendError(ChannelHandlerContext ctx, String message, HttpResponseStatus status) {
    var response = errorResponse(message, status);
    ctx.writeAndFlush(response);
  }

  private FullHttpResponse errorResponse(String message, HttpResponseStatus status) {
    var response =
        new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status, wrappedBuffer(message.getBytes()));

    response
        .headers()
        .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
        .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

    return response;
  }

  private Optional<String> queryParam(QueryStringDecoder uri, String name) {
    var params = uri.parameters().get(name);
    if (params == null || params.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(params.get(0));
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
    super.channelReadComplete(ctx);
  }
}
