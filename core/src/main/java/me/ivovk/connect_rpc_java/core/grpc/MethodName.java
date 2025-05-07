package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.MethodDescriptor;

record MethodName(
    String service,
    String method
) {

  public static MethodName from(MethodDescriptor<?, ?> descriptor) {
    return new MethodName(
        descriptor.getServiceName(),
        descriptor.getBareMethodName()
    );
  }

  public String getFullyQualifiedName() {
    return service + "/" + method;
  }

}