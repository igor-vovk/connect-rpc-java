package me.ivovk.connect_rpc_java.core.http.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import connectrpc.Code;
import connectrpc.Error;
import connectrpc.ErrorDetailsAny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectErrorAdapterTest {

  private Gson gson;

  @BeforeEach
  void setUp() {
    gson =
        new GsonBuilder()
            .registerTypeAdapter(Error.class, new ConnectErrorAdapter())
            .registerTypeAdapter(ErrorDetailsAny.class, new ErrorDetailsAnyAdapter())
            .create();
  }

  @Test
  void serializeSimpleError() {
    var error = Error.newBuilder().setCode(Code.CODE_CANCELED).build();
    var json = gson.toJson(error);
    assertEquals("{\"code\":\"canceled\"}", json);
  }

  @Test
  void deserializeSimpleError() {
    var json = "{\"code\":\"canceled\"}";
    var error = gson.fromJson(json, Error.class);
    assertEquals(Code.CODE_CANCELED, error.getCode());
  }

  @Test
  void serializeErrorWithMessage() {
    var error =
        Error.newBuilder()
            .setCode(Code.CODE_UNKNOWN)
            .setMessage("An unknown error occurred")
            .build();
    var json = gson.toJson(error);
    assertEquals("{\"code\":\"unknown\",\"message\":\"An unknown error occurred\"}", json);
  }

  @Test
  void deserializeErrorWithMessage() {
    var json = "{\"code\":\"unknown\",\"message\":\"An unknown error occurred\"}";
    var error = gson.fromJson(json, Error.class);
    assertEquals(Code.CODE_UNKNOWN, error.getCode());
    assertEquals("An unknown error occurred", error.getMessage());
  }

  @Test
  void serializeErrorWithDetails() {
    var error =
        Error.newBuilder()
            .setCode(Code.CODE_INVALID_ARGUMENT)
            .setMessage("Invalid argument provided")
            .addDetails(
                ErrorDetailsAny.newBuilder()
                    .setType("type.googleapis.com/google.rpc.BadRequest")
                    .setValue(ByteString.copyFromUtf8("detail data"))
                    .build())
            .build();
    var json = gson.toJson(error);
    var expectedJson =
        "{\"code\":\"invalid_argument\",\"message\":\"Invalid argument provided\",\"details\":[{\"type\":\"type.googleapis.com/google.rpc.BadRequest\",\"value\":\"ZGV0YWlsIGRhdGE\"}]}";
    assertEquals(expectedJson, json);
  }

  @Test
  void deserializeErrorWithDetails() {
    var json =
        "{\"code\":\"invalid_argument\",\"message\":\"Invalid argument provided\",\"details\":[{\"type\":\"type.googleapis.com/google.rpc.BadRequest\",\"value\":\"ZGV0YWlsIGRhdGE\"}]}";
    var error = gson.fromJson(json, Error.class);
    assertEquals(Code.CODE_INVALID_ARGUMENT, error.getCode());
    assertEquals("Invalid argument provided", error.getMessage());
    assertEquals(1, error.getDetailsCount());
    var detail = error.getDetails(0);
    assertEquals("type.googleapis.com/google.rpc.BadRequest", detail.getType());
    assertEquals(ByteString.copyFromUtf8("detail data"), detail.getValue());
  }

  @Test
  void roundTrip() {
    var originalError =
        Error.newBuilder()
            .setCode(Code.CODE_PERMISSION_DENIED)
            .setMessage("You shall not pass!")
            .addDetails(
                ErrorDetailsAny.newBuilder()
                    .setType("com.example.AuthFailure")
                    .setValue(ByteString.copyFrom(new byte[] {1, 2, 3}))
                    .build())
            .build();

    var json = gson.toJson(originalError);
    var deserializedError = gson.fromJson(json, Error.class);

    assertEquals(originalError, deserializedError);
  }
}
