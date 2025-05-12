package me.ivovk.connect_rpc_java.conformance.interceptors;

import io.grpc.*;

public class MetadataInterceptor implements ServerInterceptor {
  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    MetadataAccess.setRequestMetadata(headers);

    return next.startCall(new MetadataAttachingServerCall<>(call), headers);
  }

  static final class MetadataAttachingServerCall<ReqT, RespT>
      extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

    MetadataAttachingServerCall(ServerCall<ReqT, RespT> delegate) {
      super(delegate);
    }

    @Override
    public void close(Status status, Metadata trailers) {
      Metadata metadata = MetadataAccess.getResponseTrailers();
      if (metadata != null) {
        trailers.merge(metadata);
      }
      super.close(status, trailers);
    }
  }
}
