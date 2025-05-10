package me.ivovk.connect_rpc_java.netty;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.MethodDescriptor.Marshaller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.io.IOException;

public class Response {

  public static HttpResponse create(
      GeneratedMessageV3 message, Marshaller<GeneratedMessageV3> marshaller, HttpHeaders headers) {
    return create(message, marshaller, headers, HttpResponseStatus.OK);
  }

  public static HttpResponse create(
      GeneratedMessageV3 message,
      Marshaller<GeneratedMessageV3> marshaller,
      HttpHeaders headers,
      HttpResponseStatus status) {
    ByteBuf buff;
    int contentLength;
    try (var responseStream = marshaller.stream(message)) {
      var bytes = responseStream.readAllBytes();
      contentLength = bytes.length;
      buff = Unpooled.wrappedBuffer(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buff);
    response.headers().setAll(headers).set(HttpHeaderNames.CONTENT_LENGTH, contentLength);

    return response;
  }
}
