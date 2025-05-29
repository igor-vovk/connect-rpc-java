package me.ivovk.connect_rpc_java.core.http;

import static org.junit.jupiter.api.Assertions.*;

import me.ivovk.connect_rpc_java.core.http.Paths.Path;
import org.junit.jupiter.api.Test;

class PathsTest {

  @Test
  void testExtractPathSegmentsWithEmptyPath() {
    var result = Paths.extractPathSegments("");

    assertEquals(Path.of(""), result);
  }

  @Test
  void testExtractPathSegmentsWithRootPath() {
    var result = Paths.extractPathSegments("/");

    assertEquals(Path.of(""), result);
  }

  @Test
  void testExtractPathSegmentsWithSimplePath() {
    var result = Paths.extractPathSegments("/api/v1/users");

    assertEquals(Path.of("api", "v1", "users"), result);
  }

  @Test
  void testExtractPathSegmentsWithTrailingSlash() {
    var result = Paths.extractPathSegments("/api/v1/users/");

    assertEquals(Path.of("api", "v1", "users"), result);
  }

  @Test
  void testExtractPathSegmentsWithPathWithoutLeadingSlash() {
    var result = Paths.extractPathSegments("api/v1/users");

    assertEquals(Path.of("api", "v1", "users"), result);
  }

  @Test
  void testExtractPathSegmentsWithPrefix() {
    var prefix = Path.of("api", "v1");
    var result = Paths.extractPathSegments("/api/v1/users", prefix);

    assertTrue(result.isPresent());
    assertEquals(Path.of("users"), result.get());
  }

  @Test
  void testExtractPathSegmentsWithPrefixAndTrailingSlash() {
    var prefix = Path.of("api", "v1");
    var result = Paths.extractPathSegments("/api/v1/users/", prefix);

    assertTrue(result.isPresent());
    assertEquals(Path.of("users"), result.get());
  }

  @Test
  void testExtractPathSegmentsWithNonMatchingPrefix() {
    var prefix = Path.of("api", "v2");
    var result = Paths.extractPathSegments("/api/v1/users", prefix);

    assertFalse(result.isPresent());
  }

  @Test
  void testExtractPathSegmentsWithLongerPathThanPrefix() {
    var prefix = Path.of("api", "v1", "users", "profiles");
    var result = Paths.extractPathSegments("/api/v1/users", prefix);

    assertFalse(result.isPresent());
  }

  @Test
  void testExtractPathSegmentsWithRootPathPrefix() {
    var result = Paths.extractPathSegments("/api/v1/users", Path.ROOT_PATH);

    assertTrue(result.isPresent());
    assertEquals(Path.of("api", "v1", "users"), result.get());
  }

  @Test
  void testExtractPathSegmentsWithEmptyPathAndNonRootPrefix() {
    var prefix = Path.of("api");
    var result = Paths.extractPathSegments("", prefix);

    assertFalse(result.isPresent());
  }
}
