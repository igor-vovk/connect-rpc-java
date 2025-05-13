package me.ivovk.connect_rpc_java.conformance;

import static me.ivovk.connect_rpc_java.core.utils.CollectionUtils.merge;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import connectrpc.conformance.v1.ConformanceServiceGrpc;
import connectrpc.conformance.v1.Service.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.stub.StreamObserver;
import me.ivovk.connect_rpc_java.conformance.interceptors.MetadataAccess;
import me.ivovk.connect_rpc_java.core.grpc.ErrorDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConformanceServiceImpl extends ConformanceServiceGrpc.ConformanceServiceImplBase {
  record UnaryHandlerResponse(ConformancePayload payload, Metadata metadata) {}

  @Override
  public void unary(UnaryRequest request, StreamObserver<UnaryResponse> responseObserver) {
    try {
      var metadataCtx = MetadataAccess.getRequestMetadata();

      var response =
          handleUnaryRequest(
              request.getResponseDefinition(), List.of(request), metadataCtx.requestMetadata());

      metadataCtx.sendMetadata(response.metadata);
      responseObserver.onNext(UnaryResponse.newBuilder().setPayload(response.payload).build());

      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void idempotentUnary(
      IdempotentUnaryRequest request, StreamObserver<IdempotentUnaryResponse> responseObserver) {
    try {
      var metadataCtx = MetadataAccess.getRequestMetadata();

      var response =
          handleUnaryRequest(
              request.getResponseDefinition(), List.of(request), metadataCtx.requestMetadata());

      metadataCtx.sendMetadata(response.metadata);
      responseObserver.onNext(
          IdempotentUnaryResponse.newBuilder().setPayload(response.payload).build());

      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  private UnaryHandlerResponse handleUnaryRequest(
      UnaryResponseDefinition responseDefinition, List<Message> requests, Metadata metadata) {
    var requestInfo = mkRequestInfo(metadata, requests);

    var trailers =
        mkMetadata(
            merge(
                responseDefinition.getResponseHeadersList(),
                responseDefinition.getResponseTrailersList().stream()
                    .map(h -> h.toBuilder().setName("trailer-" + h.getName()).build())
                    .toList()));

    var responseData =
        switch (responseDefinition.getResponseCase()) {
          case RESPONSE_DATA -> responseDefinition.getResponseData();
          case RESPONSE_NOT_SET -> ByteString.EMPTY;
          case ERROR -> {
            var error = responseDefinition.getError();

            ErrorDetails.inject(trailers, requestInfo);

            throw Status.fromCodeValue(error.getCodeValue())
                .withDescription(error.getMessage())
                .asRuntimeException(trailers);
          }
        };

    var conformancePayload =
        ConformancePayload.newBuilder().setData(responseData).setRequestInfo(requestInfo).build();

    if (responseDefinition.getResponseDelayMs() > 0) {
      try {
        Thread.sleep(responseDefinition.getResponseDelayMs());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    return new UnaryHandlerResponse(conformancePayload, trailers);
  }

  private ConformancePayload.RequestInfo mkRequestInfo(Metadata metadata, List<Message> requests) {
    var requestInfo =
        ConformancePayload.RequestInfo.newBuilder()
            .addAllRequestHeaders(mkConformanceHeaders(metadata))
            .addAllRequests(requests.stream().map(Any::pack).toList());

    extractTimeoutMs(metadata).ifPresent(requestInfo::setTimeoutMs);

    return requestInfo.build();
  }

  private Iterable<Header> mkConformanceHeaders(Metadata metadata) {
    var list = new ArrayList<Header>();

    for (String k : metadata.keys()) {
      var values = metadata.getAll(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER));
      var header = Header.newBuilder().setName(k).addAllValue(values).build();

      list.add(header);
    }

    return list;
  }

  private Metadata mkMetadata(Iterable<Header> headers) {
    var metadata = new Metadata();
    for (Header header : headers) {
      var key = Metadata.Key.of(header.getName(), Metadata.ASCII_STRING_MARSHALLER);

      for (String value : header.getValueList()) {
        metadata.put(key, value);
      }
    }

    return metadata;
  }

  private Optional<Long> extractTimeoutMs(Metadata metadata) {
    return Optional.ofNullable(metadata.get(GrpcUtil.TIMEOUT_KEY)).map(t -> t / 1_000_000L);
  }
}
