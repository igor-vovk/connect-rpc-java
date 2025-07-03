package me.ivovk.connect_rpc_java.core.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;

public class StatusExceptionWithHeaders extends StatusException {

  private final Metadata headers;

  public StatusExceptionWithHeaders(Status status, Metadata headers, Metadata trailers) {
    super(status, trailers);
    this.headers = headers;
  }

  public Metadata getHeaders() {
    return headers;
  }
}
