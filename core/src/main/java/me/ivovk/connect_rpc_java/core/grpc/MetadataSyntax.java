package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.Metadata;

import java.util.function.Function;

public class MetadataSyntax {

  public static <T> Metadata.AsciiMarshaller<T> asciiMarshaller(
      Function<String, T> fromString, Function<T, String> toString) {
    return new Metadata.AsciiMarshaller<>() {
      @Override
      public String toAsciiString(T value) {
        return toString.apply(value);
      }

      @Override
      public T parseAsciiString(String serialized) {
        return fromString.apply(serialized);
      }
    };
  }
}
