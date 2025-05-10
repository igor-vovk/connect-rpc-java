package me.ivovk.connect_rpc_java.conformance.util;

import io.grpc.*;

/**
 * Interceptor that injects the metadata into the context for each request.
 *
 * <p>This is used to extract the metadata from the request and make it available in the context for
 * processing.
 */
public class MetadataInjectingInterceptor implements ServerInterceptor {
  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    Context.current().withValue(Constants.METADATA_KEY, headers);
    return next.startCall(call, headers);
  }
}
