package me.ivovk.connect_rpc_java.netty;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Metadata;
import me.ivovk.connect_rpc_java.core.http.MediaTypes.MediaType;

public record RequestEntity(Metadata headers, MediaType mediaType, GeneratedMessageV3 message) {}
