package me.ivovk.connect_rpc_java.core.connect;

import connectrpc.ErrorOuterClass;
import io.grpc.Status;

import java.util.Arrays;

public class StatusCodeMappings {

  private static final int[] HTTP_STATUS_CODES_BY_GRPC_CODE;
  private static final ErrorOuterClass.Code[] CONNECT_STATUS_CODES_BY_GRPC_CODE;

  static {
    int maxCode = Arrays.stream(Status.Code.values()).mapToInt(Status.Code::value).max().orElse(0);

    var httpStatusCodes = new int[maxCode + 1];
    var connectStatusCodes = new ErrorOuterClass.Code[maxCode + 1];
    for (Status.Code code : Status.Code.values()) {
      httpStatusCodes[code.value()] =
          switch (code) {
            case CANCELLED -> 499; // 499 Client Closed Request
            case UNKNOWN -> 500; // 500 Internal Server Error
            case INVALID_ARGUMENT -> 400; // 400 Bad Request
            case DEADLINE_EXCEEDED -> 504; // 504 Gateway Timeout
            case NOT_FOUND -> 404; // 404 Not Found
            case ALREADY_EXISTS -> 409; // 409 Conflict
            case PERMISSION_DENIED -> 403; // 403 Forbidden
            case RESOURCE_EXHAUSTED -> 429; // 429 Too Many Requests
            case FAILED_PRECONDITION -> 400; // 400 Bad Request
            case ABORTED -> 409; // 409 Conflict
            case OUT_OF_RANGE -> 400; // 400 Bad Request
            case UNIMPLEMENTED -> 501; // 501 Not Implemented
            case INTERNAL -> 500; // 500 Internal Server Error
            case UNAVAILABLE -> 503; // 503 Service Unavailable
            case DATA_LOSS -> 500; // 500 Internal Server Error
            case UNAUTHENTICATED -> 401; // 401 Unauthorized
            default -> 500; // 500 Internal Server Error
          };

      connectStatusCodes[code.value()] =
          switch (code) {
            case CANCELLED -> ErrorOuterClass.Code.CODE_CANCELED;
            case UNKNOWN -> ErrorOuterClass.Code.CODE_UNKNOWN;
            case INVALID_ARGUMENT -> ErrorOuterClass.Code.CODE_INVALID_ARGUMENT;
            case DEADLINE_EXCEEDED -> ErrorOuterClass.Code.CODE_DEADLINE_EXCEEDED;
            case NOT_FOUND -> ErrorOuterClass.Code.CODE_NOT_FOUND;
            case ALREADY_EXISTS -> ErrorOuterClass.Code.CODE_ALREADY_EXISTS;
            case PERMISSION_DENIED -> ErrorOuterClass.Code.CODE_PERMISSION_DENIED;
            case RESOURCE_EXHAUSTED -> ErrorOuterClass.Code.CODE_RESOURCE_EXHAUSTED;
            case FAILED_PRECONDITION -> ErrorOuterClass.Code.CODE_FAILED_PRECONDITION;
            case ABORTED -> ErrorOuterClass.Code.CODE_ABORTED;
            case OUT_OF_RANGE -> ErrorOuterClass.Code.CODE_OUT_OF_RANGE;
            case UNIMPLEMENTED -> ErrorOuterClass.Code.CODE_UNIMPLEMENTED;
            case INTERNAL -> ErrorOuterClass.Code.CODE_INTERNAL;
            case UNAVAILABLE -> ErrorOuterClass.Code.CODE_UNAVAILABLE;
            case DATA_LOSS -> ErrorOuterClass.Code.CODE_DATA_LOSS;
            case UNAUTHENTICATED -> ErrorOuterClass.Code.CODE_UNAUTHENTICATED;
            default -> ErrorOuterClass.Code.CODE_INTERNAL;
          };
    }

    HTTP_STATUS_CODES_BY_GRPC_CODE = httpStatusCodes;
    CONNECT_STATUS_CODES_BY_GRPC_CODE = connectStatusCodes;
  }

  public static int getHttpStatusCode(Status.Code code) {
    return HTTP_STATUS_CODES_BY_GRPC_CODE[code.value()];
  }

  public static ErrorOuterClass.Code getConnectStatusCode(Status.Code code) {
    return CONNECT_STATUS_CODES_BY_GRPC_CODE[code.value()];
  }
}
