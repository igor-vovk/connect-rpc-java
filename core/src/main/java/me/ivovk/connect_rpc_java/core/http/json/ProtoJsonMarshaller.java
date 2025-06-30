package me.ivovk.connect_rpc_java.core.http.json;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ProtoJsonMarshaller<T extends Message> implements MethodDescriptor.Marshaller<T> {

  private static final Charset charset = StandardCharsets.UTF_8;

  private final Printer printer;
  private final Parser parser;
  private final T defaultInstance;

  public ProtoJsonMarshaller(Printer printer, Parser parser, T defaultInstance) {
    this.printer = printer;
    this.parser = parser;
    this.defaultInstance = defaultInstance;
  }

  @Override
  public InputStream stream(T value) {
    try {
      var baos = new ByteArrayOutputStream();
      var writer = new OutputStreamWriter(baos, charset);
      printer.appendTo(value, writer);
      writer.flush();

      return new ByteArrayInputStream(baos.toByteArray());
    } catch (IOException e) {
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
}
