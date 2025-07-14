package me.ivovk.connect_rpc_java.netty.client;

import com.google.protobuf.Message;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.timeout.ReadTimeoutException;
import me.ivovk.connect_rpc_java.core.connect.StatusCodeMappings;
import me.ivovk.connect_rpc_java.core.grpc.ErrorDetails;
import me.ivovk.connect_rpc_java.core.grpc.GrpcHeaders;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/** A Netty handler for processing responses from a Connect RPC server. */
public class ConnectClientHandler<Resp extends Message>
    extends SimpleChannelInboundHandler<HttpObject> {

  private static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MethodDescriptor<?, Resp> methodDescriptor;
  private final HeaderMapping<HttpHeaders> headerMapping;
  private final JsonMarshallerFactory marshallerFactory;
  private final ClientCall.Listener<Resp> responseListener;

  public ConnectClientHandler(
      MethodDescriptor<?, Resp> methodDescriptor,
      HeaderMapping<HttpHeaders> headerMapping,
      JsonMarshallerFactory marshallerFactory,
      ClientCall.Listener<Resp> responseListener) {
    this.methodDescriptor = methodDescriptor;
    this.headerMapping = headerMapping;
    this.marshallerFactory = marshallerFactory;
    this.responseListener = responseListener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
    if (msg instanceof FullHttpResponse httpResponse) {
      if (logger.isTraceEnabled()) {
        logger.trace("<<< Response status: {}", httpResponse.status());
        logger.trace("<<< Response headers: {}", httpResponse.headers());
        logger.trace(
            "<<< Response content: {}", httpResponse.content().toString(Charset.defaultCharset()));
      }
      var metadata = headerMapping.toMetadata(httpResponse.headers());
      var hat = GrpcHeaders.splitIntoHeadersAndTrailers(metadata);

      responseListener.onHeaders(hat.headers());

      var responseStatus = httpResponse.status();
      var responseContentStream = new ByteBufInputStream(httpResponse.content(), false);

      if (responseStatus.codeClass() == HttpStatusClass.SUCCESS) {
        var defaultMessage = methodDescriptor.getResponseMarshaller().parse(EMPTY_STREAM);
        var responseMessage =
            marshallerFactory.jsonMarshaller(defaultMessage).parse(responseContentStream);

        responseListener.onMessage(responseMessage);
        responseListener.onClose(Status.OK, hat.trailers());
      } else {
        var error =
            marshallerFactory
                .jsonMarshaller(connectrpc.Error.getDefaultInstance())
                .parse(responseContentStream);

        if (logger.isTraceEnabled()) {
          logger.trace("<<< Received error response: {}", error);
        }

        var status =
            (error == null || error.getCode() == connectrpc.Code.CODE_UNSPECIFIED)
                ? StatusCodeMappings.toGrpcStatus(responseStatus.code())
                : Status.fromCodeValue(error.getCode().getNumber())
                    .withDescription(error.getMessage());

        if (error != null && error.getDetailsCount() > 0) {
          var details = error.getDetails(0);

          ErrorDetails.injectDetails(hat.trailers(), details);
        }

        responseListener.onClose(status, hat.trailers());
      }

      ctx.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (cause == ReadTimeoutException.INSTANCE) {
      responseListener.onClose(Status.DEADLINE_EXCEEDED, new Metadata());
    } else {
      responseListener.onClose(Status.fromThrowable(cause), Status.trailersFromThrowable(cause));
    }

    ctx.close();
  }
}
