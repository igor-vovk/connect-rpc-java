package me.ivovk.connect_rpc_java.netty.client;

import com.google.protobuf.Message;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import me.ivovk.connect_rpc_java.core.grpc.GrpcHeaders;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;

import java.io.ByteArrayInputStream;

public class ConnectRpcClientHandler<Resp> extends SimpleChannelInboundHandler<HttpObject> {

  private final MethodDescriptor<?, Resp> methodDescriptor;
  private final HeaderMapping<HttpHeaders> headerMapping;
  private final JsonMarshallerFactory marshallerFactory;
  private final ClientCall.Listener<Resp> listener;

  public ConnectRpcClientHandler(
      MethodDescriptor<?, Resp> methodDescriptor,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory marshallerFactory,
      ClientCall.Listener<Resp> listener) {
    this.methodDescriptor = methodDescriptor;
    this.headerMapping = headerMapping;
    this.marshallerFactory = marshallerFactory;
    this.listener = listener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    if (msg instanceof FullHttpResponse httpResponse) {
      var defaultMessage =
          methodDescriptor.getResponseMarshaller().parse(new ByteArrayInputStream(new byte[0]));
      Resp responseMessage;
      if (defaultMessage instanceof Message message) {
        responseMessage =
            (Resp)
                marshallerFactory
                    .jsonMarshaller(message)
                    .parse(new ByteArrayInputStream(httpResponse.content().array()));
      } else {
        throw new IllegalStateException(
            "Response marshaller is not a protobuf message: "
                + methodDescriptor.getResponseMarshaller());
      }

      var metadata = headerMapping.toMetadata(httpResponse.headers());
      var headersAndTrailers = GrpcHeaders.splitIntoHeadersAndTrailers(metadata);

      listener.onHeaders(headersAndTrailers.headers());

      if (httpResponse.status().code() >= 200 && httpResponse.status().code() < 300) {
        listener.onMessage(responseMessage);
        listener.onClose(Status.OK, headersAndTrailers.trailers());
      } else {
        listener.onClose(
            Status.fromCodeValue(httpResponse.status().code())
                .withDescription(httpResponse.status().reasonPhrase()),
            headersAndTrailers.trailers());
      }

      ctx.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    listener.onClose(Status.fromThrowable(cause), Status.trailersFromThrowable(cause));
    ctx.close();
  }
}
