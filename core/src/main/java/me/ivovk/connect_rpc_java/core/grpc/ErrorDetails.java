package me.ivovk.connect_rpc_java.core.grpc;

import com.google.protobuf.Message;
import connectrpc.ErrorOuterClass.ErrorDetailsAny;
import io.grpc.Metadata;

import java.util.Optional;

public final class ErrorDetails {

  public static final Metadata.Key<ErrorDetailsAny> ERROR_DETAILS_KEY =
      Metadata.Key.of(
          "grpc-status-details-bin",
          MetadataSyntax.binaryMarshaller(ErrorDetailsAny.parser(), ErrorDetailsAny::toByteArray));

  public static <M extends Message> void inject(Metadata metadata, M m) {
    var errorDetailsAny =
        ErrorDetailsAny.newBuilder()
            .setType(m.getDescriptorForType().getFullName())
            .setValue(m.toByteString())
            .build();

    metadata.discardAll(ERROR_DETAILS_KEY);
    metadata.put(ERROR_DETAILS_KEY, errorDetailsAny);
  }

  public static Optional<ErrorDetailsAny> get(Metadata metadata) {
    return Optional.ofNullable(metadata.get(ERROR_DETAILS_KEY));
  }
}
