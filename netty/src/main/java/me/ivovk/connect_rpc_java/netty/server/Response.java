package me.ivovk.connect_rpc_java.netty.server;

import com.google.protobuf.Message;
import io.grpc.MethodDescriptor.Marshaller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Response {

  private static final Logger logger = LoggerFactory.getLogger(Response.class);

  public static <M extends Message> HttpResponse create(
      M message, Marshaller<M> marshaller, HttpHeaders headers) {
    return create(message, marshaller, headers, HttpResponseStatus.OK);
  }

  public static <M extends Message> HttpResponse create(
      M message, Marshaller<M> marshaller, HttpHeaders headers, HttpResponseStatus status) {
    ByteBuf buff;
    int contentLength;
    try (var responseStream = marshaller.stream(message)) {
      var bytes = responseStream.readAllBytes();

      if (logger.isTraceEnabled()) {
        logger.trace("<<< HTTP response: {} {}", status.code(), new String(bytes));
        logger.trace("<<< Headers: {}", headers);
      }

      contentLength = bytes.length;
      buff = Unpooled.wrappedBuffer(bytes);
    } catch (IOException e) {
      logger.error("Error while reading response stream", e);
      throw new RuntimeException(e);
    }

    headers.set(HttpHeaderNames.CONTENT_LENGTH, contentLength);

    return new DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1, status, buff, headers, EmptyHttpHeaders.INSTANCE);
  }
}
