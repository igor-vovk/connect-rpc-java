package me.ivovk.connect_rpc_java.core.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MediaTypesTest {

  @Test
  void testParseValidMediaTypes() {
    assertEquals(MediaTypes.APPLICATION_JSON, MediaTypes.parse("application/json"));
    assertEquals(MediaTypes.APPLICATION_PROTO, MediaTypes.parse("application/proto"));
    assertEquals(MediaTypes.APPLICATION_JSON, MediaTypes.parse("application/json; charset=utf-8"));
  }

  @Test
  void testParseInvalidMediaType() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> MediaTypes.parse("application/xml"));
    assertTrue(exception.getMessage().contains("Unsupported media type"));
  }

  @Test
  void testParseShortValidMediaTypes() {
    assertEquals(MediaTypes.APPLICATION_JSON, MediaTypes.parseShort("json"));
    assertEquals(MediaTypes.APPLICATION_PROTO, MediaTypes.parseShort("proto"));
  }

  @Test
  void testParseShortInvalidMediaType() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> MediaTypes.parseShort("xml"));
    assertTrue(exception.getMessage().contains("Unsupported media type"));
  }
}
