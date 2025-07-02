package me.ivovk.connect_rpc_java.core.connect;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import me.ivovk.connect_rpc_java.core.grpc.ErrorDetails;
import me.ivovk.connect_rpc_java.core.grpc.StatusExceptionWithHeaders;

public class ErrorHandling {

  public record Details(
      int httpStatusCode, connectrpc.Error error, Metadata headers, Metadata trailers) {}

  public static Details extractDetails(Throwable e) {
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
    ErrorDetails.extract(trailers).ifPresent(errorBuilder::addDetails);

    return new Details(
        StatusCodeMappings.toHttpStatusCode(grpcStatus), errorBuilder.build(), headers, trailers);
  }
}
