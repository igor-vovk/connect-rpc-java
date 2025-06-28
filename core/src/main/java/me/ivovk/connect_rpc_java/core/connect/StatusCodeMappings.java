package me.ivovk.connect_rpc_java.core.connect;

import connectrpc.Code;
import io.grpc.Status;

public class StatusCodeMappings {

  public static int toHttpStatusCode(Status status) {
    return switch (status.getCode()) {
      case OK -> 200;
      case CANCELLED -> 408;
      case UNKNOWN -> 500;
      case INVALID_ARGUMENT -> 400;
      case DEADLINE_EXCEEDED -> 408;
      case NOT_FOUND -> 404;
      case ALREADY_EXISTS -> 409;
      case PERMISSION_DENIED -> 403;
      case UNAUTHENTICATED -> 401;
      case RESOURCE_EXHAUSTED -> 429;
      case FAILED_PRECONDITION -> 400;
      case ABORTED -> 409;
      case OUT_OF_RANGE -> 400;
      case UNIMPLEMENTED -> 501;
      case INTERNAL -> 500;
      case UNAVAILABLE -> 503;
      case DATA_LOSS -> 500;
    };
  }

  public static Code toConnectCode(Status status) {
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
