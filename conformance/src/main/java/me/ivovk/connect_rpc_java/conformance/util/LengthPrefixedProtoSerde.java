package me.ivovk.connect_rpc_java.conformance.util;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;

public class LengthPrefixedProtoSerde {

  private final InputStream in;
  private final OutputStream out;

  public LengthPrefixedProtoSerde(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
  }

  public static LengthPrefixedProtoSerde forSystemInOut() {
    return new LengthPrefixedProtoSerde(System.in, System.out);
  }

  @Nullable
  public <I> I read(Parser<I> parser) throws IOException {
    var requestSize = IntSerde.read(in);
    if (requestSize == null) {
      return null;
    }
    return parser.parseFrom(in.readNBytes(requestSize));
  }

  public void write(MessageLite message) throws IOException {
    IntSerde.write(out, message.getSerializedSize());
    out.flush();
    message.writeTo(out);
    out.flush();
  }
}
