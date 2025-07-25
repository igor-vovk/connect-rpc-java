package me.ivovk.connect_rpc_java.core.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import io.grpc.Metadata;

import java.util.function.Function;

public class MetadataSyntax {

  public static <T> Metadata.AsciiMarshaller<T> asciiMarshaller(
      Function<String, T> parser, Function<T, String> serializer) {
    return new Metadata.AsciiMarshaller<>() {
      @Override
      public String toAsciiString(T value) {
        return serializer.apply(value);
      }

      @Override
      public T parseAsciiString(String serialized) {
        return parser.apply(serialized);
      }
    };
  }

  public static <T> Metadata.BinaryMarshaller<T> binaryMarshaller(
      Parser<T> parser, Function<T, byte[]> serializer) {
    return new Metadata.BinaryMarshaller<>() {
      @Override
      public byte[] toBytes(T value) {
        return serializer.apply(value);
      }

      @Override
      public T parseBytes(byte[] serialized) {
        try {
          return parser.parseFrom(serialized);
        } catch (InvalidProtocolBufferException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static <T> Metadata.BinaryMarshaller<T> binaryMarshaller(
      Function<byte[], T> parser, Function<T, byte[]> serializer) {
    return new Metadata.BinaryMarshaller<>() {
      @Override
      public byte[] toBytes(T value) {
        return serializer.apply(value);
      }

      @Override
      public T parseBytes(byte[] serialized) {
        return parser.apply(serialized);
      }
    };
  }
}
