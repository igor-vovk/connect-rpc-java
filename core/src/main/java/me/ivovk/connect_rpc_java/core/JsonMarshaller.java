package me.ivovk.connect_rpc_java.core;

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

public class JsonMarshaller {

  public static <T extends Message> Marshaller<T> jsonMarshaller(final T defaultInstance) {
    Parser parser = JsonFormat.parser();
    Printer printer = JsonFormat.printer();

    return jsonMarshaller(defaultInstance, parser, printer);
  }

  public static <T extends Message> Marshaller<T> jsonMarshaller(
      T defaultInstance,
      Parser parser,
      Printer printer
  ) {
    Charset charset = StandardCharsets.UTF_8;

    return new Marshaller<>() {
      @Override
      public InputStream stream(T value) {
        try {
          return new ByteArrayInputStream(printer.print(value).getBytes(charset));
        } catch (InvalidProtocolBufferException e) {
          throw Status.INTERNAL.withDescription("Unable to print json proto")
              .withCause(e).asRuntimeException();
        }
      }

      @Override
      @SuppressWarnings("unchecked")
      public T parse(InputStream inputStream) {
        Message.Builder builder = defaultInstance.newBuilderForType();
        Reader reader = new InputStreamReader(inputStream, charset);
        T proto;

        try {
          parser.merge(reader, builder);
          proto = (T) builder.build();
          reader.close();
        } catch (IOException e) {
          throw Status.INTERNAL.withDescription("Invalid protobuf byte sequence")
              .withCause(e).asRuntimeException();
        }

        return proto;
      }
    };
  }
}
