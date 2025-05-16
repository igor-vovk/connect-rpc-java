package me.ivovk.connect_rpc_java.core.connect;

import connectrpc.Error;
import io.grpc.Metadata;
import io.grpc.Status;
import me.ivovk.connect_rpc_java.core.grpc.ErrorDetails;

import java.util.List;

public class ErrorHandling {

  public record Details(int httpStatusCode, Metadata metadata, Error error) {}

  public static Details extractErrorDetails(Throwable e) {
    var grpcStatus = Status.fromThrowable(e);

    var message = grpcStatus.getDescription();
    if (message == null) {
      message = e.getMessage();
    }

    var metadata = Status.trailersFromThrowable(e);
    if (metadata == null) {
      metadata = new Metadata();
    }

    var details = metadata.removeAll(ErrorDetails.ERROR_DETAILS_KEY);
    if (details == null) {
      details = List.of();
    }

    var httpStatusCode = StatusCodeMappings.getHttpStatusCode(grpcStatus.getCode());

    return new Details(
        httpStatusCode,
        metadata,
        Error.newBuilder()
            .setCode(StatusCodeMappings.getConnectStatusCode(grpcStatus.getCode()))
            .setMessage(message)
            .addAllDetails(details)
            .build());
  }
}
