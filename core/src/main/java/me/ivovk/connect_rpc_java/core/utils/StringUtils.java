package me.ivovk.connect_rpc_java.core.utils;

public class StringUtils {
  private StringUtils() {}

  public static String stripPrefix(String string, String prefix) {
    if (string.startsWith(prefix)) {
      return string.substring(prefix.length());
    }
    return string;
  }
}
