package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GsonMarshaller<T extends Message> implements MethodDescriptor.Marshaller<T> {

  private static final Charset charset = StandardCharsets.UTF_8;

  private final Gson gson;
  private final T defaultInstance;

  public GsonMarshaller(Gson gson, T defaultInstance) {
    this.gson = gson;
    this.defaultInstance = defaultInstance;
  }

  @Override
  public InputStream stream(T value) {
    try {
      var baos = new ByteArrayOutputStream();
      var writer = new OutputStreamWriter(baos, charset);
      gson.toJson(value, writer);
      writer.flush();

      return new ByteArrayInputStream(baos.toByteArray());
    } catch (Exception e) {
      throw Status.INTERNAL
          .withDescription("Unable to print json proto")
          .withCause(e)
          .asRuntimeException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public T parse(InputStream stream) {
    try (var reader = new InputStreamReader(stream, charset)) {
      return (T) gson.fromJson(reader, defaultInstance.getClass());
    } catch (IOException e) {
      throw Status.INTERNAL
          .withDescription("Unable to parse json")
          .withCause(e)
          .asRuntimeException();
    }
  }
}
