package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.*;
import connectrpc.Code;
import connectrpc.Error;

import java.lang.reflect.Type;

public class ConnectErrorSerializer implements JsonSerializer<Error> {

  private static final JsonElement[] STRING_ERROR_CODES = new JsonElement[16 + 1];
  private static final String CODE_PREFIX = "CODE_";

  static {
    String codeName;
    for (var code : Code.values()) {
      if (code == Code.UNRECOGNIZED) continue;

      codeName = code.name();
      if (codeName.startsWith(CODE_PREFIX)) {
        codeName = codeName.substring(CODE_PREFIX.length()).toLowerCase();
      }

      STRING_ERROR_CODES[code.getNumber()] = new JsonPrimitive(codeName);
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
}
