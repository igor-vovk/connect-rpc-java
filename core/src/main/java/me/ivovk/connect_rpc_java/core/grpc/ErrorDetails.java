package me.ivovk.connect_rpc_java.core.grpc;

import com.google.protobuf.Message;
import connectrpc.ErrorDetailsAny;
import io.grpc.Metadata;

import java.util.Optional;

public final class ErrorDetails {

  private static final Metadata.Key<ErrorDetailsAny> ERROR_DETAILS_KEY =
      Metadata.Key.of(
          "connect-error-details-bin",
          MetadataSyntax.binaryMarshaller(ErrorDetailsAny.parser(), ErrorDetailsAny::toByteArray));

  public static <M extends Message> void inject(Metadata metadata, M m) {
    var errorDetailsAny =
        ErrorDetailsAny.newBuilder()
            .setType(m.getDescriptorForType().getFullName())
            .setValue(m.toByteString())
            .build();

    injectDetails(metadata, errorDetailsAny);
  }

  public static void injectDetails(Metadata metadata, ErrorDetailsAny eda) {
    metadata.discardAll(ERROR_DETAILS_KEY);
    metadata.put(ERROR_DETAILS_KEY, eda);
  }

  public static Optional<ErrorDetailsAny> extract(Metadata metadata) {
    var details = metadata.get(ERROR_DETAILS_KEY);
    if (details != null) {
      metadata.discardAll(ERROR_DETAILS_KEY);
    }

    return Optional.ofNullable(details);
  }
}
