package me.ivovk.connect_rpc_java.core.http;

import io.grpc.Metadata;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public interface HeaderMapping<H> {

  Metadata toMetadata(H headers);

  H toHeaders(Metadata metadata);

  H trailersToHeaders(Metadata trailers);

  Predicate<String> DEFAULT_INCOMING_HEADERS_FILTER =
      name -> !(name.startsWith("Connection") || name.startsWith("connection"));

  Predicate<String> DEFAULT_OUTGOING_HEADERS_FILTER = name -> !name.startsWith("grpc-");

  Map<String, Metadata.Key<String>> KEY_CACHE = new WeakHashMap<>();

  static Metadata.Key<String> cachedAsciiKey(String name) {
    return KEY_CACHE.computeIfAbsent(
        name, k -> Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER));
  }

  static Metadata.Key<String> metadataKeyByHeaderName(String name) {
    return switch (name) {
      case "User-Agent", "user-agent" -> cachedAsciiKey("x-user-agent");
      default -> cachedAsciiKey(name);
    };
  }
}
