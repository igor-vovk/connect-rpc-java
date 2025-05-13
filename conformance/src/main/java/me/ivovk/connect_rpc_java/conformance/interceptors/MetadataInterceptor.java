package me.ivovk.connect_rpc_java.conformance.interceptors;

import io.grpc.*;

import java.util.concurrent.atomic.AtomicReference;

public class MetadataInterceptor implements ServerInterceptor {
  public record MetadataCtx(Metadata requestMetadata, AtomicReference<Metadata> responseMetadata) {
    public void sendMetadata(Metadata metadata) {
      responseMetadata.set(metadata);
    }
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    var metadataCtx = new MetadataCtx(headers, new AtomicReference<>(null));
    var context = MetadataAccess.setRequestMetadata(metadataCtx);

    return Contexts.interceptCall(
        context, new MetadataAttachingServerCall<>(call, metadataCtx), headers, next);
  }

  static final class MetadataAttachingServerCall<ReqT, RespT>
      extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

    private final MetadataCtx metadataCtx;

    MetadataAttachingServerCall(ServerCall<ReqT, RespT> delegate, MetadataCtx metadataCtx) {
      super(delegate);

      this.metadataCtx = metadataCtx;
    }

    @Override
    public void close(Status status, Metadata trailers) {
      var headersToSet = metadataCtx.responseMetadata.get();
      if (headersToSet != null) {
        trailers.merge(headersToSet);
      }

      super.close(status, trailers);
    }
  }
}
