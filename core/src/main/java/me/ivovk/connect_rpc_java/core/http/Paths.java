package me.ivovk.connect_rpc_java.core.http;

import static me.ivovk.connect_rpc_java.core.utils.StringUtils.stripPrefix;

import java.util.Arrays;
import java.util.Optional;

public class Paths {

  public record Path(String[] segments) {
    public static Path ROOT_PATH = new Path(new String[0]);

    public static Path of(String... segments) {
      return new Path(segments);
    }

    public int length() {
      return segments.length;
    }

    @Override
    public String toString() {
      return "Path{" + "segments=" + Arrays.toString(segments) + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      Path path = (Path) o;
      return Arrays.equals(segments, path.segments);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(segments);
    }
  }

  public static Optional<Path> extractPathSegments(String path) {
    return extractPathSegments(path, Path.ROOT_PATH);
  }

  public static Optional<Path> extractPathSegments(String path, Path prefix) {
    var pathSegments = stripPrefix(path, "/").split("/");

    if (prefix.segments.length == 0) {
      return Optional.of(new Path(pathSegments));
    } else {
      return dropPrefix(new Path(pathSegments), prefix);
    }
  }

  private static Optional<Path> dropPrefix(Path path, Path prefix) {
    if (path.segments.length < prefix.segments.length) {
      return Optional.empty();
    }

    for (int i = 0; i < prefix.segments.length; i++) {
      if (!path.segments[i].equals(prefix.segments[i])) {
        return Optional.empty();
      }
    }
    var newSegments =
        Arrays.copyOfRange(path.segments, prefix.segments.length, path.segments.length);

    return Optional.of(new Path(newSegments));
  }
}
