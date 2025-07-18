package me.ivovk.connect_rpc_java.conformance;

import connectrpc.conformance.v1.*;
import me.ivovk.connect_rpc_java.conformance.interceptors.MetadataInterceptor;
import me.ivovk.connect_rpc_java.conformance.util.LengthPrefixedProtoSerde;
import me.ivovk.connect_rpc_java.netty.ConnectNettyServerBuilder;
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

  private final LengthPrefixedProtoSerde serde;

  public NettyServerLauncher(LengthPrefixedProtoSerde serde) {
    this.serde = serde;
  }

  public void run() throws Exception {
    // Must read the request from STDIN, even though it is not used.
    serde.read(ServerCompatRequest.getDefaultInstance());

    var service = new ConformanceServiceImpl();

    var server =
        ConnectNettyServerBuilder.forServices(service)
            .serverBuilderConfigurer(sb -> sb.intercept(new MetadataInterceptor()))
            // Registering message types in TypeRegistry is required to pass
            // google.protobuf.Any JSON-serialization conformance tests
            .jsonTypeRegistryConfigurer(
                b ->
                    b.add(UnaryRequest.getDescriptor()).add(IdempotentUnaryRequest.getDescriptor()))
            .build();

    serde.write(
        ServerCompatResponse.newBuilder()
            .setHost(server.getHost())
            .setPort(server.getPort())
            .build());

    System.err.println("Netty Server started on " + server.getHost() + ":" + server.getPort());
    logger.info("Netty Server started on {}:{}", server.getHost(), server.getPort());

    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
  }
}
