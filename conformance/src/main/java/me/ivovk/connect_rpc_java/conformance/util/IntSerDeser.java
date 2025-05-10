package me.ivovk.connect_rpc_java.conformance.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class IntSerDeser {

  private static final int INT_SIZE = 4;

  private IntSerDeser() {}

  public static int read(InputStream in) throws IOException {
    return ByteBuffer.wrap(in.readNBytes(INT_SIZE)).getInt();
  }

  public static void write(OutputStream out, int i) throws IOException {
    out.write(ByteBuffer.allocate(INT_SIZE).putInt(i).array());
  }
}
