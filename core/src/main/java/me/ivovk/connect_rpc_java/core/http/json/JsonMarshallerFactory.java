package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import connectrpc.Error;
import connectrpc.ErrorDetailsAny;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.Status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <a
 * href="https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/advanced/JsonMarshaller.java">source</a>
 */
public class JsonMarshallerFactory {

  private static final Charset charset = StandardCharsets.UTF_8;

  private final Parser parser;
  private final Printer printer;
  private final Gson gson;

  public JsonMarshallerFactory() {
    this(TypeRegistry.getEmptyTypeRegistry());
  }

  public JsonMarshallerFactory(TypeRegistry typeRegistry) {
    this.parser = JsonFormat.parser().usingTypeRegistry(typeRegistry);
    this.printer = JsonFormat.printer().usingTypeRegistry(typeRegistry);

    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(ErrorDetailsAny.class, new ErrorDetailsAnySerializer())
            .registerTypeAdapter(Error.class, new ConnectErrorSerializer())
            .create();
  }

  public <T extends Message> Marshaller<T> jsonMarshaller(final T defaultInstance) {
    if (defaultInstance instanceof Error) {
      return marshallerUsingGson();
    } else {
      return marshallerUsingProtoJsonFormat(defaultInstance);
    }
  }

  private <T extends Message> Marshaller<T> marshallerUsingGson() {
    return new Marshaller<>() {
      @Override
      public InputStream stream(T value) {
        try {
          return new ByteArrayInputStream(gson.toJson(value).getBytes(charset));
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
          return (T) gson.fromJson(reader, Error.class);
        } catch (IOException e) {
          throw Status.INTERNAL
              .withDescription("Unable to parse json")
              .withCause(e)
              .asRuntimeException();
        }
      }
    };
  }

  private <T extends Message> Marshaller<T> marshallerUsingProtoJsonFormat(T defaultInstance) {
    return new Marshaller<>() {
      @Override
      public InputStream stream(T value) {
        try {
          return new ByteArrayInputStream(printer.print(value).getBytes(charset));
        } catch (InvalidProtocolBufferException e) {
          throw Status.INTERNAL
              .withDescription("Unable to print json proto")
              .withCause(e)
              .asRuntimeException();
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public T parse(InputStream stream) {
        var builder = defaultInstance.newBuilderForType();

        try (var reader = new InputStreamReader(stream, charset)) {
          parser.merge(reader, builder);

          return (T) builder.build();
        } catch (IOException e) {
          throw Status.INTERNAL
              .withDescription("Unable to parse json")
              .withCause(e)
              .asRuntimeException();
        }
      }
    };
  }
}
