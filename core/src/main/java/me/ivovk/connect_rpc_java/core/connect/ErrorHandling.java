package me.ivovk.connect_rpc_java.core.connect;

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
    Status grpcStatus = Status.INTERNAL;
    Metadata headers = new Metadata();
    Metadata trailers = new Metadata();
    String message = e.getMessage();

    Throwable cause = e;
    while (cause != null) {
      if (cause instanceof StatusException se) {
        grpcStatus = se.getStatus();
        if (se instanceof StatusExceptionWithHeaders seh) {
          headers = seh.getHeaders();
        }
        if (se.getTrailers() != null) {
          trailers = se.getTrailers();
        }
        if (grpcStatus.getDescription() != null) {
          message = grpcStatus.getDescription();
        } else if (se.getMessage() != null) {
          message = se.getMessage();
        }

        break;
      } else if (cause instanceof StatusRuntimeException sre) {
        grpcStatus = sre.getStatus();
        if (sre.getTrailers() != null) {
          trailers = sre.getTrailers();
        }
        if (grpcStatus.getDescription() != null) {
          message = grpcStatus.getDescription();
        } else if (sre.getMessage() != null) {
          message = sre.getMessage();
        }

        break;
      }

      cause = cause.getCause();
    }

    var errorBuilder =
        connectrpc.Error.newBuilder()
            .setCode(StatusCodeMappings.toConnectCode(grpcStatus))
            .setMessage(message);

    // Extract details from trailers if present
    var errorDetails = trailers.removeAll(GrpcHeaders.ERROR_DETAILS_KEY);
    if (errorDetails != null) {
      for (var errorDetail : errorDetails) {
        errorBuilder.addDetails(
            ErrorDetailsAny.newBuilder()
                .setType(errorDetail.getTypeUrl().replace("type.googleapis.com/", ""))
                .setValue(errorDetail.getValue())
                .build());
      }
    }

    return new ErrorDetails(
        StatusCodeMappings.toHttpStatusCode(grpcStatus), errorBuilder.build(), headers, trailers);
  }
}
