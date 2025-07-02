package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.*;
import connectrpc.Code;
import connectrpc.Error;
import connectrpc.ErrorDetailsAny;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConnectErrorAdapter implements JsonSerializer<Error>, JsonDeserializer<Error> {

  private static final JsonElement[] STRING_ERROR_CODES = new JsonElement[16 + 1];
  private static final String CODE_PREFIX = "CODE_";
  private static final Map<String, Code> STRING_TO_CODE = new HashMap<>();

  static {
    String codeName;
    for (var code : Code.values()) {
      if (code == Code.UNRECOGNIZED) continue;

      codeName = code.name();
      if (codeName.startsWith(CODE_PREFIX)) {
        codeName = codeName.substring(CODE_PREFIX.length()).toLowerCase();
      }

      STRING_ERROR_CODES[code.getNumber()] = new JsonPrimitive(codeName);
      STRING_TO_CODE.put(codeName, code);
    }
  }

  @Override
  public JsonElement serialize(Error src, Type typeOfSrc, JsonSerializationContext context) {
    var result = new JsonObject();

    var code = src.getCode();
    result.add("code", STRING_ERROR_CODES[code.getNumber()]);

    if (src.hasMessage()) {
      result.addProperty("message", src.getMessage());
    }

    if (src.getDetailsCount() > 0) {
      var detailsArray = new JsonArray();

      for (var detail : src.getDetailsList()) {
        var detailJson = context.serialize(detail);
        detailsArray.add(detailJson);
      }

      result.add("details", detailsArray);
    }

    return result;
  }

  @Override
  public Error deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    var jsonObject = json.getAsJsonObject();
    var builder = Error.newBuilder();

    var codeElement = jsonObject.get("code");
    if (codeElement != null && codeElement.isJsonPrimitive()) {
      var codeString = codeElement.getAsString();
      var code = STRING_TO_CODE.get(codeString);
      if (code != null) {
        builder.setCode(code);
      }
    }

    var messageElement = jsonObject.get("message");
    if (messageElement != null && messageElement.isJsonPrimitive()) {
      builder.setMessage(messageElement.getAsString());
    }

    var detailsElement = jsonObject.get("details");
    if (detailsElement != null && detailsElement.isJsonArray()) {
      var detailsArray = detailsElement.getAsJsonArray();
      for (var detailElement : detailsArray) {
        var detail = (ErrorDetailsAny) context.deserialize(detailElement, ErrorDetailsAny.class);
        builder.addDetails(detail);
      }
    }

    return builder.build();
  }
}
