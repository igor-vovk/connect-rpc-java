package me.ivovk.connect_rpc_java.conformance.interceptors;

import io.grpc.Context;

public class MetadataAccess {
  private static final Context.Key<MetadataInterceptor.MetadataCtx> REQUEST_METADATA_KEY =
      Context.key("metadata");

  public static MetadataInterceptor.MetadataCtx getRequestMetadata() {
    return REQUEST_METADATA_KEY.get();
  }

  public static Context setRequestMetadata(MetadataInterceptor.MetadataCtx ctx) {
    return Context.current().withValue(REQUEST_METADATA_KEY, ctx);
  }
}
