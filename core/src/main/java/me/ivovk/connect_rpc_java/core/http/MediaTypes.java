package me.ivovk.connect_rpc_java.core.http;

import java.util.List;

public class MediaTypes {

  private MediaTypes() {}

  public record MediaType(String type, String subtype) {
    public static MediaType of(String type, String subtype) {
      return new MediaType(type, subtype);
    }

    public String toString() {
      return type + "/" + subtype;
    }
  }

  public static final MediaType APPLICATION_JSON = MediaType.of("application", "json");

  public static final MediaType APPLICATION_PROTO = MediaType.of("application", "proto");

  public static final List<MediaType> SUPPORTED_MEDIA_TYPES =
      List.of(APPLICATION_JSON, APPLICATION_PROTO);

  public static MediaType parse(String s) throws IllegalArgumentException {
    return switch (s) {
      case "application/json" -> APPLICATION_JSON;
      case "application/proto" -> APPLICATION_PROTO;
      default -> throw new IllegalArgumentException("Unsupported media type: " + s);
    };
  }

  public static MediaType parseShort(String s) throws IllegalArgumentException {
    return switch (s) {
      case "json" -> APPLICATION_JSON;
      case "proto" -> APPLICATION_PROTO;
      default -> throw new IllegalArgumentException("Unsupported media type: " + s);
    };
  }
}
