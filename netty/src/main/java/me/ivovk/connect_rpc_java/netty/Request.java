package me.ivovk.connect_rpc_java.netty;

import io.grpc.Metadata;

public record Request(Metadata headers) {}
