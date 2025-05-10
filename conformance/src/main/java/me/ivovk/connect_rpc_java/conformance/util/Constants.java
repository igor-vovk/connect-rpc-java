package me.ivovk.connect_rpc_java.conformance.util;

import io.grpc.Context;
import io.grpc.Metadata;

public class Constants {
  public static final Context.Key<Metadata> METADATA_KEY = Context.key("metadata");
}
