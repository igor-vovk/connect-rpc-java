package me.ivovk.connect_rpc_java.netty.connect;

import connectrpc.ErrorOuterClass;
import io.grpc.MethodDescriptor.Marshaller;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import me.ivovk.connect_rpc_java.core.connect.ErrorHandling;
import me.ivovk.connect_rpc_java.core.http.MediaTypes;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;
import me.ivovk.connect_rpc_java.netty.NettyHeaderMapping;
import me.ivovk.connect_rpc_java.netty.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ConnectErrorHandler {

  private static final Marshaller<ErrorOuterClass.Error> PROTO_MARSHALLER =
      new Marshaller<>() {
        @Override
        public InputStream stream(ErrorOuterClass.Error value) {
          return new ByteArrayInputStream(value.toByteArray());
        }

        @Override
        public ErrorOuterClass.Error parse(InputStream stream) {
          try {
            return ErrorOuterClass.Error.parseFrom(stream);
          } catch (Exception e) {
            throw new RuntimeException("Failed to parse Error", e);
          }
        }
      };

  private static final Marshaller<ErrorOuterClass.Error> JSON_MARSHALLER =
      new JsonMarshallerFactory().jsonMarshaller(ErrorOuterClass.Error.getDefaultInstance());

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final NettyHeaderMapping headerMapping;

  public ConnectErrorHandler(NettyHeaderMapping headerMapping) {
    this.headerMapping = headerMapping;
  }

  public HttpResponse handle(Throwable e, MediaTypes.MediaType mediaType) {
    var details = ErrorHandling.extractErrorDetails(e);
    var headers = headerMapping.trailersToHeaders(details.metadata());

    if (logger.isTraceEnabled()) {
      logger.trace(
          "<<< HTTP status: {}, Connect Error Code {}",
          details.httpStatusCode(),
          details.error().getCode());
      logger.trace("<<< Error processing request", e);
    }

    Marshaller<ErrorOuterClass.Error> marshaller;
    if (mediaType == MediaTypes.APPLICATION_JSON) {
      marshaller = JSON_MARSHALLER;
    } else if (mediaType == MediaTypes.APPLICATION_PROTO) {
      marshaller = PROTO_MARSHALLER;
    } else {
      throw new IllegalArgumentException("Unsupported media type: " + mediaType);
    }

    return Response.create(
        details.error(), marshaller, headers, HttpResponseStatus.valueOf(details.httpStatusCode()));
  }
}
