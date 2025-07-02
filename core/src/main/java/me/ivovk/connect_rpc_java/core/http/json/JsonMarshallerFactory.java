package me.ivovk.connect_rpc_java.core.http.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Message;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import connectrpc.Error;
import connectrpc.ErrorDetailsAny;
import io.grpc.MethodDescriptor.Marshaller;

/**
 * <a
 * href="https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/advanced/JsonMarshaller.java">source</a>
 */
public class JsonMarshallerFactory {

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
            .registerTypeAdapter(ErrorDetailsAny.class, new ErrorDetailsAnyAdapter())
            .registerTypeAdapter(Error.class, new ConnectErrorAdapter())
            .create();
  }

  public <T extends Message> Marshaller<T> jsonMarshaller(T defaultInstance) {
    if (defaultInstance instanceof Error) {
      return new GsonMarshaller<>(gson, defaultInstance);
    } else {
      return new ProtoJsonMarshaller<>(printer, parser, defaultInstance);
    }
  }
}
