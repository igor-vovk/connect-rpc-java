package me.ivovk.connect_rpc_java.core.http;

import io.grpc.MethodDescriptor;
import me.ivovk.connect_rpc_java.core.http.MediaTypes.MediaType;

import java.util.Map;

public class MarshallerRegistry {

  private final Map<MediaType, MethodDescriptor.Marshaller<?>> marshallers;

  public MarshallerRegistry(Map<MediaType, MethodDescriptor.Marshaller<?>> marshallers) {
    this.marshallers = marshallers;
  }

  public MethodDescriptor.Marshaller<?> getMarshaller(MediaType mediaType) {
    return marshallers.get(mediaType);
  }
}
