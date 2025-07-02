package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.ByteString;
import connectrpc.ErrorDetailsAny;

import java.io.IOException;
import java.util.Base64;

public class ErrorDetailsAnyTypeAdapter extends TypeAdapter<ErrorDetailsAny> {

  private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

  @Override
  public void write(JsonWriter out, ErrorDetailsAny value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name("type").value(value.getType());
    out.name("value").value(BASE64_ENCODER.encodeToString(value.getValue().toByteArray()));
    out.endObject();
  }

  @Override
  public ErrorDetailsAny read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    in.beginObject();
    String type = null;
    ByteString value = null;
    while (in.hasNext()) {
      switch (in.nextName()) {
        case "type":
          type = in.nextString();
          break;
        case "value":
          value = ByteString.copyFrom(BASE64_DECODER.decode(in.nextString()));
          break;
        default:
          in.skipValue();
          break;
      }
    }
    in.endObject();

    if (type == null) {
      throw new IOException("Missing 'type' field in ErrorDetailsAny JSON");
    }
    if (value == null) {
      throw new IOException("Missing 'value' field in ErrorDetailsAny JSON");
    }

    return ErrorDetailsAny.newBuilder().setType(type).setValue(value).build();
  }
}
