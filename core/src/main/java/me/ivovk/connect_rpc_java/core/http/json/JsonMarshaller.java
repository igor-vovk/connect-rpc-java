package me.ivovk.connect_rpc_java.core.http.json;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.Status;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <a href="https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/advanced/JsonMarshaller.java">source</a>
 */
public class JsonMarshaller {

  final static Charset charset = StandardCharsets.UTF_8;

  private JsonMarshaller() {
  }

  public static <T extends Message> Marshaller<T> jsonMarshaller(final T defaultInstance) {
    final Parser parser = JsonFormat.parser();
    final Printer printer = JsonFormat.printer();

    return jsonMarshaller(defaultInstance, parser, printer);
  }

  public static <T extends Message> Marshaller<T> jsonMarshaller(
      final T defaultInstance,
      final Parser parser,
      final Printer printer
  ) {
    return new Marshaller<>() {
      @Override
      public InputStream stream(T value) {
        try {
          return new ByteArrayInputStream(printer.print(value).getBytes(charset));
        } catch (InvalidProtocolBufferException e) {
          throw Status.INTERNAL.withDescription("Unable to print json proto").withCause(e)
              .asRuntimeException();
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public T parse(InputStream stream) {
        Message.Builder builder = defaultInstance.newBuilderForType();

        try (Reader reader = new InputStreamReader(stream, charset)) {
          parser.merge(reader, builder);

          return (T) builder.build();
        } catch (IOException e) {
          throw Status.INTERNAL.withDescription("Unable to parse json").withCause(e)
              .asRuntimeException();
        }
      }
    };
  }

}
