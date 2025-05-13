package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.Metadata;

public class GrpcHeaders {

  public static final Metadata.Key<String> X_USER_AGENT =
      Metadata.Key.of("x-user-agent", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<String> X_TEST_CASE_NAME =
      Metadata.Key.of("x-test-case-name", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<Long> CONNECT_TIMEOUT_MS =
      Metadata.Key.of(
          "connect-timeout-ms", MetadataSyntax.asciiMarshaller(Long::parseLong, String::valueOf));
}
