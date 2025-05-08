package me.ivovk.connect_rpc_java.core.http;

import static me.ivovk.connect_rpc_java.core.utils.StringUtils.stripPrefix;

public class Paths {

  public record Path(String[] segments) {
    static Path ROOT_PATH = new Path(new String[0]);
  }

  public static Path extractPathSegments(String path) {
    return extractPathSegments(path, Path.ROOT_PATH);
  }

  public static Path extractPathSegments(String path, Path prefix) {
    var pathSegments = stripPrefix(path, "/").split("/");

    if (prefix.segments.length == 0) {
      return new Path(pathSegments);
    }

    return null;
  }
}
