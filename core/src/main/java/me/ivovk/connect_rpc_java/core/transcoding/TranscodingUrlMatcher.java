package me.ivovk.connect_rpc_java.core.transcoding;

import com.google.api.HttpRule;
import me.ivovk.connect_rpc_java.core.grpc.MethodRegistry;
import me.ivovk.connect_rpc_java.core.http.Paths;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TranscodingUrlMatcher {

  public static TranscodingUrlMatcher create(
      List<MethodRegistry.Entry> methods, Paths.Path pathPrefix) {
    methods.stream()
        .flatMap(method -> method.httpRule().stream())
        .flatMap(
            httpRule -> {
              var additionalBindings = httpRule.getAdditionalBindingsList();

              return Stream.concat(Stream.of(httpRule), additionalBindings.stream())
                  .map(
                      rule -> {
                        var methodAndPattern = extractMethodAndPattern(rule);

                        return null;
                      });
            });

    return null;
  }

  record MethodAndPattern(Optional<HttpRule.PatternCase> method, Paths.Path path) {}

  private static MethodAndPattern extractMethodAndPattern(HttpRule rule) {
    switch (rule.getPatternCase()) {
      case GET -> {
        return new MethodAndPattern(
            Optional.of(HttpRule.PatternCase.GET), Paths.extractPathSegments(rule.getGet()));
      }
      case PUT -> {
        return new MethodAndPattern(
            Optional.of(HttpRule.PatternCase.PUT), Paths.extractPathSegments(rule.getPut()));
      }
      case POST -> {
        return new MethodAndPattern(
            Optional.of(HttpRule.PatternCase.POST), Paths.extractPathSegments(rule.getPost()));
      }
      case DELETE -> {
        return new MethodAndPattern(
            Optional.of(HttpRule.PatternCase.DELETE), Paths.extractPathSegments(rule.getDelete()));
      }
      case PATCH -> {
        return new MethodAndPattern(
            Optional.of(HttpRule.PatternCase.PATCH), Paths.extractPathSegments(rule.getPatch()));
      }
      case CUSTOM -> {
        return new MethodAndPattern(
            Optional.empty(), Paths.extractPathSegments(rule.getCustom().getPath()));
      }
      case PATTERN_NOT_SET -> throw new IllegalStateException("Pattern not set");
      default -> throw new IllegalStateException("Unexpected value: " + rule.getPatternCase());
    }
  }
}
