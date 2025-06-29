package me.ivovk.connect_rpc_java.conformance.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;

public class IntSerde {

  private static final int INT_SIZE = 4;

  private IntSerde() {}

  @Nullable
  public static Integer read(InputStream in) throws IOException {
    var bytes = in.readNBytes(INT_SIZE);
    if (bytes.length < INT_SIZE) {
      return null;
    }
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static void write(OutputStream out, int i) throws IOException {
    out.write(ByteBuffer.allocate(INT_SIZE).putInt(i).array());
  }
}
