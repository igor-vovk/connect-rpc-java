package me.ivovk.connect_rpc_java.conformance;

public class Launcher {

  public static void main(String... args) throws Exception {
    if (args.length == 0) {
      System.err.println("Please provide the launcher argument");
      System.exit(1);
    }
    var launcher = args[0];

    switch (launcher) {
      case "netty-server" -> NettyServerLauncher.main(args);
      case "netty-client" -> NettyClientLauncher.main(args);
      default -> System.err.println("Unknown launcher: " + launcher);
    }
  }
}
