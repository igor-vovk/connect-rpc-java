package me.ivovk.connect_rpc_java.core.connect;

import static java.util.Objects.requireNonNull;

import connectrpc.ErrorDetailsAny;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import me.ivovk.connect_rpc_java.core.grpc.GrpcHeaders;
import me.ivovk.connect_rpc_java.core.grpc.StatusExceptionWithHeaders;

public class ErrorHandling {

  public record ErrorDetails(
      int httpStatusCode, connectrpc.Error error, Metadata headers, Metadata trailers) {}

  public static ErrorDetails extractDetails(Throwable e) {
    Status grpcStatus;
    Metadata headers;
    Metadata trailers;
    String message;

    if (e instanceof StatusException se) {
      grpcStatus = se.getStatus();
      if (se instanceof StatusExceptionWithHeaders seh) {
        headers = seh.getHeaders();
      } else {
        headers = new Metadata();
      }
      trailers = requireNonNull(se.getTrailers());
      message = se.getStatus().getDescription();
    } else if (e instanceof StatusRuntimeException sre) {
      grpcStatus = sre.getStatus();
      headers = new Metadata();
      trailers = requireNonNull(sre.getTrailers());
      message = sre.getStatus().getDescription();
    } else {
      grpcStatus = Status.INTERNAL;
      headers = new Metadata();
      trailers = new Metadata();
      message = e.getMessage();
    }

    var errorBuilder =
        connectrpc.Error.newBuilder()
            .setCode(StatusCodeMappings.toConnectCode(grpcStatus))
            .setMessage(message);

    // Extract details from trailers if present
    var errorDetails = trailers.get(GrpcHeaders.ERROR_DETAILS_KEY);
    if (errorDetails != null) {
      errorBuilder.addDetails(
          ErrorDetailsAny.newBuilder()
              .setType(errorDetails.getTypeUrl().replace("type.googleapis.com/", ""))
              .setValue(errorDetails.getValue())
              .build());
    }

    return new ErrorDetails(
        StatusCodeMappings.toHttpStatusCode(grpcStatus), errorBuilder.build(), headers, trailers);
  }
}
