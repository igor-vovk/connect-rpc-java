package me.ivovk.connect_rpc_java.core.grpc;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;
import me.ivovk.connect_rpc_java.core.http.MediaTypes;
import me.ivovk.connect_rpc_java.core.http.json.JsonMarshallerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodRegistry {

  public static class Entry {
    private final MethodName methodName;
    private final MethodDescriptor<Message, Message> descriptor;
    private final MethodDescriptor<Message, Message> jsonDescriptor;
    private final Optional<HttpRule> httpRule;

    public Entry(
        MethodName methodName,
        MethodDescriptor<Message, Message> descriptor,
        MethodDescriptor<Message, Message> jsonDescriptor,
        Optional<HttpRule> httpRule) {
      this.methodName = methodName;
      this.descriptor = descriptor;
      this.jsonDescriptor = jsonDescriptor;
      this.httpRule = httpRule;
    }

    public Marshaller<Message> requestMarshaller(MediaTypes.MediaType mediaType) {
      return descriptorByMediaType(mediaType).getRequestMarshaller();
    }

    public Marshaller<Message> responseMarshaller(MediaTypes.MediaType mediaType) {
      return descriptorByMediaType(mediaType).getResponseMarshaller();
    }

    private MethodDescriptor<Message, Message> descriptorByMediaType(
        MediaTypes.MediaType mediaType) {
      if (mediaType.equals(MediaTypes.APPLICATION_JSON)) {
        return jsonDescriptor;
      } else if (mediaType.equals(MediaTypes.APPLICATION_PROTO)) {
        return descriptor;
      } else {
        throw new IllegalArgumentException("Unsupported media type: " + mediaType);
      }
    }

    public MethodDescriptor<Message, Message> descriptor() {
      return descriptor;
    }

    public MethodDescriptor.MethodType methodType() {
      return descriptor.getType();
    }

    public MethodName methodName() {
      return methodName;
    }

    public Optional<HttpRule> httpRule() {
      return httpRule;
    }
  }

  private final List<Entry> entries;
  private final Map<String, Map<String, Entry>> byServiceAndMethod;

  private MethodRegistry(List<Entry> entries) {
    this.entries = entries;
    this.byServiceAndMethod =
        entries.stream()
            .collect(
                Collectors.groupingBy(
                    e -> e.methodName.service(),
                    Collectors.toMap(e -> e.methodName.method(), e -> e)));
  }

  public static MethodRegistry create(
      List<ServerServiceDefinition> services, JsonMarshallerFactory jsonMarshallerFactory) {
    var entries =
        services.stream()
            .flatMap(ssd -> ssd.getMethods().stream())
            .map(
                smd -> {
                  @SuppressWarnings("unchecked")
                  var descriptor = (MethodDescriptor<Message, Message>) smd.getMethodDescriptor();

                  var methodName = MethodName.from(descriptor);

                  var jsonDescriptor =
                      descriptor.toBuilder()
                          .setRequestMarshaller(
                              jsonMarshallerFactory.jsonMarshaller(
                                  getMessagePrototype(descriptor.getRequestMarshaller())))
                          .setResponseMarshaller(
                              jsonMarshallerFactory.jsonMarshaller(
                                  getMessagePrototype(descriptor.getResponseMarshaller())))
                          .build();

                  return new Entry(
                      methodName, descriptor, jsonDescriptor, extractHttpRule(descriptor));
                })
            .toList();

    return new MethodRegistry(entries);
  }

  /**
   * Extracts the HTTP rule from the method descriptor.
   *
   * @param descriptor The method descriptor.
   * @return An optional containing the HTTP rule if present, otherwise an empty optional.
   */
  private static Optional<HttpRule> extractHttpRule(MethodDescriptor<?, ?> descriptor) {
    if (descriptor.getSchemaDescriptor() instanceof ProtoMethodDescriptorSupplier supplier) {
      var methodDescriptor = supplier.getMethodDescriptor();
      var options = methodDescriptor.getOptions();

      if (options.hasExtension(AnnotationsProto.http)) {
        return Optional.of(options.getExtension(AnnotationsProto.http));
      }
    }

    return Optional.empty();
  }

  protected static <T> T getMessagePrototype(Marshaller<T> marshaller) {
    if (marshaller instanceof MethodDescriptor.PrototypeMarshaller<T> prototypeMarshaller) {
      return prototypeMarshaller.getMessagePrototype();
    } else {
      throw new IllegalArgumentException("Marshaller is not a PrototypeMarshaller");
    }
  }

  public List<Entry> all() {
    return entries;
  }

  public Optional<Entry> get(String service, String method) {
    var methods = byServiceAndMethod.get(service);
    if (methods == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(methods.get(method));
  }
}
