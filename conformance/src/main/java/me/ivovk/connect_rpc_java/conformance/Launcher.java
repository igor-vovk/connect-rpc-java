package me.ivovk.connect_rpc_java.conformance;

import me.ivovk.connect_rpc_java.conformance.util.LengthPrefixedProtoSerde;

public class Launcher {

  public static void main(String... args) throws Exception {
    if (args.length == 0) {
      System.err.println("Please provide the launcher argument");
      System.exit(1);
    }
    var launcher = args[0];

    var serde = LengthPrefixedProtoSerde.forSystemInOut();

    switch (launcher) {
      case "netty-server" -> new NettyServerLauncher(serde).run();
      case "netty-client" -> new NettyClientLauncher(serde).run();
      default -> System.err.println("Unknown launcher: " + launcher);
    }
  }
}
