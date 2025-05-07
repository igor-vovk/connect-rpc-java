package me.ivovk.connect_rpc_java.core.grpc;

import com.google.api.AnnotationsProto;
import com.google.api.Http;
import com.google.api.HttpProto;
import com.google.api.HttpRule;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodRegistry {

  public record Entry(
      MethodName methodName,
      MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> descriptor,
      Optional<HttpRule> httpRule
  ) {
  }

  private final List<Entry> entries;
  private Map<String, Map<String, Entry>> byServiceAndMethod;

  private MethodRegistry(List<Entry> entries) {
    this.entries = entries;
    this.byServiceAndMethod = entries.stream()
        .collect(
            Collectors.groupingBy(
                entry -> entry.methodName.service(),
                Collectors.toMap(
                    entry -> entry.methodName.method(),
                    entry -> entry
                )
            )
        );
  }

  public static MethodRegistry create(List<ServerServiceDefinition> services) {
    var entries = services.stream()
        .flatMap(s -> s.getMethods().stream())
        .map(smd -> {
          var descriptor = ((ServerMethodDefinition<GeneratedMessageV3, GeneratedMessageV3>) smd).getMethodDescriptor();
          var methodName = new MethodName(descriptor.getServiceName(), descriptor.getBareMethodName());

          return new Entry(
              methodName,
              descriptor,
              extractHttpRule(descriptor)
          );
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
