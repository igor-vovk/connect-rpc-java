package me.ivovk.connect_rpc_java.core.connect;

import connectrpc.Code;
import io.grpc.Status;

public class StatusCodeMappings {

  public static int toHttpStatusCode(Status status) {
    return switch (status.getCode()) {
      case OK -> 200;
      case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE -> 400;
      case UNAUTHENTICATED -> 401;
      case NOT_FOUND -> 404;
      case PERMISSION_DENIED -> 403;
      case ALREADY_EXISTS, ABORTED -> 409;
      case RESOURCE_EXHAUSTED -> 429;
      case CANCELLED -> 499;
      case UNKNOWN, INTERNAL, DATA_LOSS -> 500;
      case UNIMPLEMENTED -> 501;
      case DEADLINE_EXCEEDED -> 504;
      case UNAVAILABLE -> 503;
    };
  }

  public static Status toGrpcStatus(int httpCode) {
    return switch (httpCode) {
      case 200 -> Status.OK;
      case 400, 500 -> Status.INTERNAL;
      case 401 -> Status.UNAUTHENTICATED;
      case 403 -> Status.PERMISSION_DENIED;
      case 404, 501 -> Status.UNIMPLEMENTED;
      case 409 -> Status.UNKNOWN;
      case 429, 502, 503, 504 -> Status.UNAVAILABLE;
      case 499 -> Status.CANCELLED;
      default ->
          Status.UNKNOWN.withDescription(
              "HTTP status code " + httpCode + " is not supported by the protocol");
    };
  }

  public static connectrpc.Code toConnectCode(Status status) {
    return switch (status.getCode()) {
      case OK -> Code.CODE_UNSPECIFIED;
      case CANCELLED -> Code.CODE_CANCELED;
      case UNKNOWN -> Code.CODE_UNKNOWN;
      case INVALID_ARGUMENT -> Code.CODE_INVALID_ARGUMENT;
      case DEADLINE_EXCEEDED -> Code.CODE_DEADLINE_EXCEEDED;
      case NOT_FOUND -> Code.CODE_NOT_FOUND;
      case ALREADY_EXISTS -> Code.CODE_ALREADY_EXISTS;
      case PERMISSION_DENIED -> Code.CODE_PERMISSION_DENIED;
      case UNAUTHENTICATED -> Code.CODE_UNAUTHENTICATED;
      case RESOURCE_EXHAUSTED -> Code.CODE_RESOURCE_EXHAUSTED;
      case FAILED_PRECONDITION -> Code.CODE_FAILED_PRECONDITION;
      case ABORTED -> Code.CODE_ABORTED;
      case OUT_OF_RANGE -> Code.CODE_OUT_OF_RANGE;
      case UNIMPLEMENTED -> Code.CODE_UNIMPLEMENTED;
      case INTERNAL -> Code.CODE_INTERNAL;
      case UNAVAILABLE -> Code.CODE_UNAVAILABLE;
      case DATA_LOSS -> Code.CODE_DATA_LOSS;
    };
  }
}
