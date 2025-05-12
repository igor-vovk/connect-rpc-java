package me.ivovk.connect_rpc_java.conformance.interceptors;

import io.grpc.Context;
import io.grpc.Metadata;

import java.util.Optional;
import javax.annotation.Nullable;

public class MetadataAccess {
  private static final Context.Key<Metadata> REQUEST_METADATA_KEY = Context.key("metadata");
  private static final Context.Key<Metadata> RESPONSE_TRAILERS_KEY = Context.key("trailers");

  public static Metadata getRequestMetadata() {
    return Optional.ofNullable(REQUEST_METADATA_KEY.get()).orElseGet(Metadata::new);
  }

  public static void setRequestMetadata(Metadata metadata) {
    Context.current().withValue(REQUEST_METADATA_KEY, metadata);
  }

  @Nullable
  public static Metadata getResponseTrailers() {
    return RESPONSE_TRAILERS_KEY.get();
  }

  public static void setResponseTrailers(Metadata trailers) {
    Context.current().withValue(RESPONSE_TRAILERS_KEY, trailers);
  }
}
