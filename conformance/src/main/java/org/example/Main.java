package org.example;

import me.ivovk.connect_rpc_java.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    var builder = NettyServerBuilder.forServices(List.of()).setPort(8080);

    try (var server = builder.build()) {
      logger.info("Server started on port {}", server.getPort());

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      server.shutdown();
                      logger.info("Server shut down");
                    } catch (Exception e) {
                      logger.error("Error shutting down server", e);
                    }
                  }));
    }
  }
}
