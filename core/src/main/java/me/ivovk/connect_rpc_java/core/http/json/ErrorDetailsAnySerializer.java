package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.*;
import connectrpc.ErrorDetailsAny;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * Serializes ErrorDetailsAny objects to JSON format according to Connect protocol specifications.
 * Based on the Connect RPC standard, ErrorDetailsAny uses a different JSON serialization format
 * compared to standard protobuf Any messages.
 */
public class ErrorDetailsAnySerializer implements JsonSerializer<ErrorDetailsAny> {

  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();

  @Override
  public JsonElement serialize(
      ErrorDetailsAny src, Type typeOfSrc, JsonSerializationContext context) {
    var jsonObject = new JsonObject();

    // Add the type field
    jsonObject.add("type", new JsonPrimitive(src.getType()));

    // Add the value field as base64-encoded string without padding
    var valueBase64 = BASE64_ENCODER.encodeToString(src.getValue().toByteArray());
    jsonObject.add("value", new JsonPrimitive(valueBase64));

    return jsonObject;
  }
}
