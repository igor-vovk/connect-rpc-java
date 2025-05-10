package me.ivovk.connect_rpc_java.conformance;

import connectrpc.conformance.v1.ServerCompat;
import me.ivovk.connect_rpc_java.conformance.util.MetadataInjectingInterceptor;
import me.ivovk.connect_rpc_java.conformance.util.ServerCompatSerDeser;
import me.ivovk.connect_rpc_java.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flow:
 *
 * <p>- Upon launch, `ServerCompatRequest` message is sent from the test runner to the server to
 * STDIN. - Server is started and listens on a random port. - `ServerCompatResponse` is sent from
 * the server to STDOUT, which instructs the test runner on which port the server is listening.
 *
 * <p>All diagnostics should be written to STDERR.
 *
 * <p>Useful links:
 *
 * <p><a
 * href="https://github.com/connectrpc/conformance/blob/main/docs/configuring_and_running_tests.md">...</a>
 */
public class NettyServerLauncher {

  private static final Logger logger = LoggerFactory.getLogger(NettyServerLauncher.class);

  public static void main(String[] args) throws Exception {
    var req = ServerCompatSerDeser.readRequest(System.in);

    var service = new ConformanceServiceImpl();

    var server =
        NettyServerBuilder.forServices(service)
            .serverBuilderConfigurer(
                sb -> {
                  sb.intercept(new MetadataInjectingInterceptor());

                  return sb;
                })
            .build();

    var resp =
        ServerCompat.ServerCompatResponse.newBuilder()
            .setHost(server.getHost())
            .setPort(server.getPort())
            .build();

    ServerCompatSerDeser.writeResponse(System.out, resp);

    System.err.println("Server started on " + server.getHost() + ":" + server.getPort());
    logger.info("Server started on {}:{}", server.getHost(), server.getPort());

    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
  }
}
