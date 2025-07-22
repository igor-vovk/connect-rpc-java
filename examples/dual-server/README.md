# Dual Server Example

This example demonstrates how to run both a ConnectRPC server and a traditional gRPC server simultaneously on different ports, with proper shutdown handling.

## What it does

- Starts a ConnectRPC server on port 8080
- Starts a traditional gRPC server on port 9090
- Both servers implement the same `GreetService` but return slightly different messages to distinguish them
- Implements proper shutdown hooks to gracefully stop both servers when the application is terminated

## Running the example

```bash
./gradlew :examples:dual-server:run
```

## Testing the servers

### Testing ConnectRPC server (port 8080)
You can test the ConnectRPC server using curl:

```bash
curl -X POST http://localhost:8080/me.ivovk.connect_rpc_java.examples.dual_server.GreetService/Greet \
  -H "Content-Type: application/json" \
  -d '{"name": "World"}'
```

### Testing gRPC server (port 9090)
You can test the traditional gRPC server using grpcurl or any gRPC client:

```bash
grpcurl -plaintext -d '{"name": "World"}' localhost:9090 me.ivovk.connect_rpc_java.examples.dual_server.GreetService/Greet
```

## Use Cases

This pattern is useful when you need to:
- Migrate gradually from gRPC to ConnectRPC
- Support both internal gRPC clients and external HTTP/JSON clients
- Provide backward compatibility while adopting new protocols
