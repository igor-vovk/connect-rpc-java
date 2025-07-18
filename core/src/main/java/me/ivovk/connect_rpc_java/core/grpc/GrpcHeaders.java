package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.Metadata;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;

public class GrpcHeaders {

  public static final Metadata.Key<String> X_USER_AGENT =
      Metadata.Key.of("x-user-agent", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<String> X_TEST_CASE_NAME =
      Metadata.Key.of("x-test-case-name", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<Long> CONNECT_TIMEOUT_MS =
      Metadata.Key.of(
          "connect-timeout-ms", MetadataSyntax.asciiMarshaller(Long::parseLong, String::valueOf));

  public record HeadersAndTrailers(Metadata headers, Metadata trailers) {}

  public static HeadersAndTrailers splitIntoHeadersAndTrailers(Metadata metadata) {
    var headers = new Metadata();
    var trailers = new Metadata();

    metadata
        .keys()
        .forEach(
            name -> {
              var key = HeaderMapping.metadataKeyByHeaderName(name);

              if (name.startsWith("trailer-")) {
                var trailerKey =
                    HeaderMapping.metadataKeyByHeaderName(name.substring("trailer-".length()));

                metadata.getAll(key).forEach(value -> trailers.put(trailerKey, value));
              } else {
                metadata.getAll(key).forEach(value -> headers.put(key, value));
              }
            });

    return new HeadersAndTrailers(headers, trailers);
  }
}
