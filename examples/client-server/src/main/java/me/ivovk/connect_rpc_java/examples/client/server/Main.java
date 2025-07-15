package me.ivovk.connect_rpc_java.examples.client.server;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import me.ivovk.connect_rpc_java.examples.client.server.gen.GreetRequest;
import me.ivovk.connect_rpc_java.examples.client.server.gen.GreetResponse;
import me.ivovk.connect_rpc_java.examples.client.server.gen.GreetServiceGrpc;
import me.ivovk.connect_rpc_java.netty.ConnectNettyChannelBuilder;
import me.ivovk.connect_rpc_java.netty.ConnectNettyServerBuilder;
import me.ivovk.connect_rpc_java.netty.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException, InterruptedException {
    // Start the ConnectRPC server
    NettyServer server =
        ConnectNettyServerBuilder.forServices(
                new GreetServiceGrpc.GreetServiceImplBase() {
                  @Override
                  public void greet(
                      GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
                    GreetResponse response =
                        GreetResponse.newBuilder()
                            .setGreeting("Hello, " + request.getName() + "!")
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                  }
                })
            .port(8080)
            .build();

    logger.info("Server started, listening on {}", server.getPort());

    // Create a ConnectRPC client
    ManagedChannel channel = ConnectNettyChannelBuilder.forAddress("localhost", 8080).build();

    GreetServiceGrpc.GreetServiceBlockingStub stub = GreetServiceGrpc.newBlockingStub(channel);

    // Make a call
    try {
      GreetRequest request = GreetRequest.newBuilder().setName("World").build();
      GreetResponse response = stub.greet(request);
      logger.info("Client received greeting: {}", response.getGreeting());
    } catch (Exception e) {
      logger.error("Client call failed: ", e);
    } finally {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
      logger.info("Client channel shut down");
    }

    // Shut down the server
    server.shutdown();
    logger.info("Server shut down");
  }
}
