package me.ivovk.connect_rpc_java.conformance;

import connectrpc.conformance.v1.ConformanceServiceGrpc;
import connectrpc.conformance.v1.Service.*;
import io.grpc.Metadata;
import io.grpc.internal.GrpcUtil;
import io.grpc.stub.StreamObserver;
import me.ivovk.connect_rpc_java.conformance.util.Constants;

import java.util.List;
import java.util.Optional;

public class ConformanceServiceImpl extends ConformanceServiceGrpc.ConformanceServiceImplBase {
  record UnaryHandlerResponse(ConformancePayload payload, Metadata metadata) {}

  @Override
  public void unary(UnaryRequest request, StreamObserver<UnaryResponse> responseObserver) {
    var metadata = Constants.METADATA_KEY.get();

    var response = handleUnaryRequest(request.getResponseDefinition(), metadata);

    responseObserver.onNext(UnaryResponse.newBuilder().setPayload(response.payload).build());
    responseObserver.onCompleted();
  }

  @Override
  public void idempotentUnary(
      IdempotentUnaryRequest request, StreamObserver<IdempotentUnaryResponse> responseObserver) {
    var metadata = Constants.METADATA_KEY.get();

    var response = handleUnaryRequest(request.getResponseDefinition(), metadata);

    responseObserver.onNext(
        IdempotentUnaryResponse.newBuilder().setPayload(response.payload).build());
    responseObserver.onCompleted();
  }

  private UnaryHandlerResponse handleUnaryRequest(
      UnaryResponseDefinition responseDefinition, Metadata ctx) {
    var requestInfo =
        ConformancePayload.RequestInfo.newBuilder().addAllRequestHeaders(mkConformanceHeaders(ctx));

    extractTimeoutMs(ctx).ifPresent(requestInfo::setTimeoutMs);

    var conformancePayload =
        ConformancePayload.newBuilder().setRequestInfo(requestInfo.build()).build();

    return new UnaryHandlerResponse(conformancePayload, ctx);
  }

  private List<Header> mkConformanceHeaders(Metadata metadata) {
    return metadata.keys().stream()
        .map(
            k -> {
              return Header.newBuilder()
                  .setName(k)
                  .addAllValue(
                      metadata.getAll(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER)))
                  .build();
            })
        .toList();
  }

  private Optional<Long> extractTimeoutMs(Metadata metadata) {
    return Optional.ofNullable(metadata.get(GrpcUtil.TIMEOUT_KEY)).map(t -> t / 1_000_000L);
  }
}
