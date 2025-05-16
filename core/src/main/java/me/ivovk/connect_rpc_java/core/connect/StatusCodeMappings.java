package me.ivovk.connect_rpc_java.core.connect;

import connectrpc.Code;
import io.grpc.Status;

import java.util.Arrays;

public class StatusCodeMappings {

  private static final int[] HTTP_STATUS_CODES_BY_GRPC_CODE;
  private static final Code[] CONNECT_STATUS_CODES_BY_GRPC_CODE;

  static {
    int maxCode = Arrays.stream(Status.Code.values()).mapToInt(Status.Code::value).max().orElse(0);

    var httpStatusCodes = new int[maxCode + 1];
    var connectStatusCodes = new Code[maxCode + 1];
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
            case CANCELLED -> Code.CODE_CANCELED;
            case UNKNOWN -> Code.CODE_UNKNOWN;
            case INVALID_ARGUMENT -> Code.CODE_INVALID_ARGUMENT;
            case DEADLINE_EXCEEDED -> Code.CODE_DEADLINE_EXCEEDED;
            case NOT_FOUND -> Code.CODE_NOT_FOUND;
            case ALREADY_EXISTS -> Code.CODE_ALREADY_EXISTS;
            case PERMISSION_DENIED -> Code.CODE_PERMISSION_DENIED;
            case RESOURCE_EXHAUSTED -> Code.CODE_RESOURCE_EXHAUSTED;
            case FAILED_PRECONDITION -> Code.CODE_FAILED_PRECONDITION;
            case ABORTED -> Code.CODE_ABORTED;
            case OUT_OF_RANGE -> Code.CODE_OUT_OF_RANGE;
            case UNIMPLEMENTED -> Code.CODE_UNIMPLEMENTED;
            case INTERNAL -> Code.CODE_INTERNAL;
            case UNAVAILABLE -> Code.CODE_UNAVAILABLE;
            case DATA_LOSS -> Code.CODE_DATA_LOSS;
            case UNAUTHENTICATED -> Code.CODE_UNAUTHENTICATED;
            default -> Code.CODE_INTERNAL;
          };
    }

    HTTP_STATUS_CODES_BY_GRPC_CODE = httpStatusCodes;
    CONNECT_STATUS_CODES_BY_GRPC_CODE = connectStatusCodes;
  }

  public static int getHttpStatusCode(Status.Code code) {
    return HTTP_STATUS_CODES_BY_GRPC_CODE[code.value()];
  }

  public static Code getConnectStatusCode(Status.Code code) {
    return CONNECT_STATUS_CODES_BY_GRPC_CODE[code.value()];
  }
}
