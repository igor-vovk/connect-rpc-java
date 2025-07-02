package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.protobuf.ByteString;
import connectrpc.ErrorDetailsAny;
import java.lang.reflect.Type;
import java.util.Base64;

/**
 * Serializes and deserializes ErrorDetailsAny objects to JSON format according to Connect protocol
 * specifications. Based on the Connect RPC standard, ErrorDetailsAny uses a different JSON
 * serialization format compared to standard protobuf Any messages.
 */
public class ErrorDetailsAnyAdapter
    implements JsonSerializer<ErrorDetailsAny>, JsonDeserializer<ErrorDetailsAny> {

  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

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

  @Override
  public ErrorDetailsAny deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    var jsonObject = json.getAsJsonObject();

    var typeElement = jsonObject.get("type");
    if (typeElement == null || !typeElement.isJsonPrimitive()) {
      throw new JsonParseException("Missing or invalid 'type' field in ErrorDetailsAny JSON");
    }
    var type = typeElement.getAsString();

    var valueElement = jsonObject.get("value");
    if (valueElement == null || !valueElement.isJsonPrimitive()) {
      throw new JsonParseException("Missing or invalid 'value' field in ErrorDetailsAny JSON");
    }
    var valueBase64 = valueElement.getAsString();
    var value = ByteString.copyFrom(BASE64_DECODER.decode(valueBase64));

    return ErrorDetailsAny.newBuilder().setType(type).setValue(value).build();
  }
}