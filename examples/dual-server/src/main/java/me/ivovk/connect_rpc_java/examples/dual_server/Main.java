package me.ivovk.connect_rpc_java.examples.dual_server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import me.ivovk.connect_rpc_java.examples.dual_server.gen.GreetRequest;
import me.ivovk.connect_rpc_java.examples.dual_server.gen.GreetResponse;
import me.ivovk.connect_rpc_java.examples.dual_server.gen.GreetServiceGrpc;
import me.ivovk.connect_rpc_java.netty.ConnectNettyServerBuilder;
import me.ivovk.connect_rpc_java.netty.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final int CONNECT_RPC_PORT = 8080;
  private static final int GRPC_PORT = 9090;

  private static NettyServer connectRpcServer;
  private static Server grpcServer;

  public static void main(String[] args) throws IOException, InterruptedException {
    // Add shutdown hook to ensure proper cleanup
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Shutdown signal received, stopping servers...");
                  stopServers();
                }));

    // Start ConnectRPC server
    startConnectRpcServer();

    // Start traditional gRPC server
    startGrpcServer();

    logger.info("Both servers are running. Press Ctrl+C to stop.");

    // Keep the application running
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      logger.info("Main thread interrupted, shutting down...");
      stopServers();
    }
  }

  private static void startConnectRpcServer() throws IOException, InterruptedException {
    connectRpcServer =
        ConnectNettyServerBuilder.forServices(
                new GreetServiceGrpc.GreetServiceImplBase() {
                  @Override
                  public void greet(
                      GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
                    logger.info("ConnectRPC server received request: {}", request.getName());
                    GreetResponse response =
                        GreetResponse.newBuilder()
                            .setGreeting("Hello from ConnectRPC, " + request.getName() + "!")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                  }
                })
            .port(CONNECT_RPC_PORT)
            .build();

    logger.info("ConnectRPC server started on port {}", CONNECT_RPC_PORT);
  }

  private static void startGrpcServer() throws IOException, InterruptedException {
    grpcServer =
        ServerBuilder.forPort(GRPC_PORT)
            .addService(
                new GreetServiceGrpc.GreetServiceImplBase() {
                  @Override
                  public void greet(
                      GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
                    logger.info("gRPC server received request: {}", request.getName());
                    GreetResponse response =
                        GreetResponse.newBuilder()
                            .setGreeting("Hello from gRPC, " + request.getName() + "!")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                  }
                })
            .build()
            .start();

    logger.info("gRPC server started on port {}", GRPC_PORT);
  }

  private static void stopServers() {
    // Stop ConnectRPC server
    if (connectRpcServer != null) {
      try {
        connectRpcServer.shutdown();
        logger.info("ConnectRPC server stopped");
      } catch (Exception e) {
        logger.error("Error stopping ConnectRPC server", e);
      }
    }

    // Stop gRPC server
    if (grpcServer != null) {
      try {
        grpcServer.shutdown();
        if (!grpcServer.awaitTermination(5, TimeUnit.SECONDS)) {
          logger.warn("gRPC server did not terminate gracefully, forcing shutdown");
          grpcServer.shutdownNow();
          if (!grpcServer.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.error("gRPC server did not terminate");
          }
        }
        logger.info("gRPC server stopped");
      } catch (InterruptedException e) {
        logger.error("Error stopping gRPC server", e);
        grpcServer.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}
