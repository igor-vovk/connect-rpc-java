package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import connectrpc.Code;
import connectrpc.Error;
import connectrpc.ErrorDetailsAny;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ErrorTypeAdapter extends TypeAdapter<Error> {

  private static final String[] CODE_TO_STRING = new String[17];
  private static final Map<String, Code> STRING_TO_CODE = new HashMap<>();

  private final TypeAdapter<ErrorDetailsAny> errorDetailsAnyAdapter =
      new ErrorDetailsAnyTypeAdapter();

  static {
    String CODE_PREFIX = "CODE_";
    String codeName;
    for (var code : Code.values()) {
      if (code == Code.UNRECOGNIZED) continue;

      codeName = code.name();
      if (codeName.startsWith(CODE_PREFIX)) {
        codeName = codeName.substring(CODE_PREFIX.length()).toLowerCase();
      } else {
        codeName = codeName.toLowerCase();
      }

      CODE_TO_STRING[code.getNumber()] = codeName;
      STRING_TO_CODE.put(codeName, code);
    }
  }

  public ErrorTypeAdapter() {}

  @Override
  public void write(JsonWriter out, Error value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name("code").value(CODE_TO_STRING[value.getCode().getNumber()]);
    if (value.hasMessage()) {
      out.name("message").value(value.getMessage());
    }
    if (value.getDetailsCount() > 0) {
      out.name("details");
      out.beginArray();
      for (var detail : value.getDetailsList()) {
        errorDetailsAnyAdapter.write(out, detail);
      }
      out.endArray();
    }
    out.endObject();
  }

  @Override
  public Error read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    var builder = Error.newBuilder();
    in.beginObject();
    while (in.hasNext()) {
      switch (in.nextName()) {
        case "code":
          var codeString = in.nextString();
          var code = STRING_TO_CODE.get(codeString);
          if (code != null) {
            builder.setCode(code);
          }
          break;
        case "message":
          builder.setMessage(in.nextString());
          break;
        case "details":
          in.beginArray();
          while (in.hasNext()) {
            ErrorDetailsAny detail = errorDetailsAnyAdapter.read(in);
            if (detail != null) {
              builder.addDetails(detail);
            }
          }
          in.endArray();
          break;
        default:
          in.skipValue();
          break;
      }
    }
    in.endObject();

    return builder.build();
  }
}
