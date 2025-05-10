package me.ivovk.connect_rpc_java.core.grpc;

import static me.ivovk.connect_rpc_java.core.http.json.JsonMarshaller.jsonMarshaller;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;
import me.ivovk.connect_rpc_java.core.http.MediaTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodRegistry {

  public record Entry(
      MethodName methodName,
      MethodDescriptor<Message, Message> descriptor,
      MethodDescriptor<Message, Message> jsonDescriptor,
      Optional<HttpRule> httpRule) {

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
  }

  private final List<Entry> entries;
  private final Map<String, Map<String, Entry>> byServiceAndMethod;

  private MethodRegistry(List<Entry> entries) {
    this.entries = entries;
    this.byServiceAndMethod =
        entries.stream()
            .collect(
                Collectors.groupingBy(
                    entry -> entry.methodName.service(),
                    Collectors.toMap(entry -> entry.methodName.method(), entry -> entry)));
  }

  public static MethodRegistry create(List<ServerServiceDefinition> services) {
    var entries =
        services.stream()
            .flatMap(s -> s.getMethods().stream())
            .map(
                smd -> {
                  @SuppressWarnings("unchecked")
                  var descriptor = (MethodDescriptor<Message, Message>) smd.getMethodDescriptor();

                  var methodName =
                      new MethodName(descriptor.getServiceName(), descriptor.getBareMethodName());

                  var jsonDescriptor =
                      descriptor.toBuilder()
                          .setRequestMarshaller(
                              jsonMarshaller(
                                  getMessagePrototype(descriptor.getRequestMarshaller())))
                          .setResponseMarshaller(
                              jsonMarshaller(
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
