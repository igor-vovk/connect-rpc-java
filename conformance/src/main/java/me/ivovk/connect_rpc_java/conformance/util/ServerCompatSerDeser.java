package me.ivovk.connect_rpc_java.conformance.util;

import connectrpc.conformance.v1.ServerCompat.ServerCompatRequest;
import connectrpc.conformance.v1.ServerCompat.ServerCompatResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServerCompatSerDeser {

  public static ServerCompatRequest readRequest(InputStream in) throws IOException {
    var requestSize = IntSerDeser.read(in);

    return ServerCompatRequest.parseFrom(in.readNBytes(requestSize));
  }

  public static void writeResponse(OutputStream out, ServerCompatResponse response)
      throws IOException {
    IntSerDeser.write(out, response.getSerializedSize());
    out.flush();
    out.write(response.toByteArray());
    out.flush();
  }
}
