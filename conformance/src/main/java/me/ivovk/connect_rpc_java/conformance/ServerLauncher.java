package me.ivovk.connect_rpc_java.conformance;

import connectrpc.conformance.v1.*;
import me.ivovk.connect_rpc_java.conformance.interceptors.MetadataInterceptor;
import me.ivovk.connect_rpc_java.conformance.util.LengthPrefixedProtoSerde;
import me.ivovk.connect_rpc_java.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
public class ServerLauncher {

  private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);

  public static void main(String[] args) throws Exception {
    var serde = LengthPrefixedProtoSerde.forSystemInOut();
    var req = serde.read(ServerCompatRequest.parser());

    var service = new ConformanceServiceImpl();

    var server =
        NettyServerBuilder.forServices(service)
            .serverBuilderConfigurer(
                sb -> {
                  // sb.intercept(new ErrorLoggingInterceptor());
                  return sb.intercept(new MetadataInterceptor());
                })
            // Registering message types in TypeRegistry is required to pass
            // com.google.protobuf.any.Any
            // JSON-serialization conformance tests
            .jsonTypeRegistryConfigurer(
                b ->
                    b.add(
                        List.of(
                            UnaryRequest.getDescriptor(), IdempotentUnaryRequest.getDescriptor())))
            .build();

    var resp =
        ServerCompatResponse.newBuilder()
            .setHost(server.getHost())
            .setPort(server.getPort())
            .build();

    serde.write(resp);

    System.err.println("Netty Server started on " + server.getHost() + ":" + server.getPort());
    logger.info("Netty Server started on {}:{}", server.getHost(), server.getPort());

    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
  }
}
