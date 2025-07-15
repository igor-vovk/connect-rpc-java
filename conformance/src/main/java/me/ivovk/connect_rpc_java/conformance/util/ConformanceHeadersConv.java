package me.ivovk.connect_rpc_java.conformance.util;

import connectrpc.conformance.v1.Header;
import io.grpc.Metadata;

import java.util.List;
import java.util.stream.Collectors;

public class ConformanceHeadersConv {

  public static Metadata toMetadata(List<Header> headers) {
    Metadata metadata = new Metadata();
    for (Header header : headers) {
      var key = Metadata.Key.of(header.getName(), Metadata.ASCII_STRING_MARSHALLER);

      for (String value : header.getValueList()) {
        metadata.put(key, value);
      }
    }
    return metadata;
  }

  public static List<Header> toHeaderList(Metadata metadata) {
    return metadata.keys().stream()
        .map(
            key ->
                Header.newBuilder()
                    .setName(key)
                    .addAllValue(
                        metadata.getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)))
                    .build())
        .collect(Collectors.toList());
  }
}
