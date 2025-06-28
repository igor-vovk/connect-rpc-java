package me.ivovk.connect_rpc_java.conformance;

public class Launcher {

  public static void main(String... args) throws Exception {
    var launcher = System.getenv("LAUNCHER");

    switch (launcher) {
      case "netty-server" -> NettyServerLauncher.main(args);
      case "netty-client" -> NettyClientLauncher.main(args);
      default -> System.err.println("Unknown launcher: " + launcher);
    }
  }
}
