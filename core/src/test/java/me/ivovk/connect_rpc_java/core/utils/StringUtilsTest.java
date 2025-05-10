package me.ivovk.connect_rpc_java.core.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void testStripPrefix() {
    assertEquals("test", StringUtils.stripPrefix("/test", "/"));
    assertEquals("test", StringUtils.stripPrefix("test", ""));
    assertEquals("st", StringUtils.stripPrefix("test", "te"));
  }
}
