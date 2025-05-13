package me.ivovk.connect_rpc_java.netty;

import com.google.protobuf.Message;
import io.grpc.Metadata;
import me.ivovk.connect_rpc_java.core.http.MediaTypes.MediaType;

public record RequestEntity(Metadata headerMetadata, MediaType mediaType, Message message) {}
