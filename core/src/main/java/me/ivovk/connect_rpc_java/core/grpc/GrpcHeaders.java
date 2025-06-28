package me.ivovk.connect_rpc_java.core.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Metadata;

public class GrpcHeaders {

  public static final Metadata.Key<String> X_USER_AGENT =
      Metadata.Key.of("x-user-agent", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<String> X_TEST_CASE_NAME =
      Metadata.Key.of("x-test-case-name", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata.Key<Long> CONNECT_TIMEOUT_MS =
      Metadata.Key.of(
          "connect-timeout-ms", MetadataSyntax.asciiMarshaller(Long::parseLong, String::valueOf));

  public static Metadata.Key<Any> ERROR_DETAILS_KEY =
      Metadata.Key.of(
          "connect-rpc-error-details",
          new Metadata.BinaryMarshaller<>() {
            @Override
            public byte[] toBytes(Any value) {
              return value.toByteArray();
            }

            @Override
            public Any parseBytes(byte[] serialized) {
              try {
                return Any.parseFrom(serialized);
              } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
              }
            }
          });

  public record HeadersAndTrailers(Metadata headers, Metadata trailers) {}

  public static HeadersAndTrailers splitIntoHeadersAndTrailers(Metadata metadata) {
    var headers = new Metadata();
    var trailers = new Metadata();

    metadata
        .keys()
        .forEach(
            name -> {
              var key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);

              if (name.startsWith("trailer-")) {
                var trailerKey =
                    Metadata.Key.of(name.substring(8), Metadata.ASCII_STRING_MARSHALLER);

                metadata.getAll(key).forEach(value -> trailers.put(trailerKey, value));
              } else {
                metadata.getAll(key).forEach(value -> headers.put(key, value));
              }
            });

    return new HeadersAndTrailers(headers, trailers);
  }
}
