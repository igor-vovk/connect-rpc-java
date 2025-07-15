# REST API / GRPC Transcoding for GRPC services written in Java

The library allows exposing GRPC services as REST-APIs using Connect protocol (with JSON messages) + GRPC Transcoding,
without Envoy or any other proxy.

In essence, a service implementing the following protobuf definition:

```protobuf
syntax = "proto3";

package example;

service ExampleService {
  rpc GetExample(GetExampleRequest) returns (GetExampleResponse) {}
}

message GetExampleRequest {
  string id = 1;
}

message GetExampleResponse {
  string name = 1;
}
```

Is exposed to the clients as a REST API:

```http
POST /example.ExampleService/GetExample HTTP/1.1
Content-Type: application/json

{
  "id": "123"
}

HTTP/1.1 200 OK

{
  "name": "example"
}
```

It is compatible with Connect protocol clients (e.g., generated with [Connect RPC](https://connectrpc.com) `protoc` and
`buf` plugins).

## Use cases

* Expose existing GRPC services as REST APIs without modifying the original service code alongside GRPC. GRPC services
  are used for internal communication, while REST APIs are used for external clients.
* Fully switch GRPC servers and clients to ConnectRPC protocol, while keeping the original GRPC service interfaces.
* Build from scratch using Connect RPC instead of GRPC, but still use the same service interfaces.

## Usage

![Maven Central](https://img.shields.io/maven-central/v/me.ivovk/connect-rpc-java-netty?style=flat-square&color=green)

Dependency for Maven:

```xml

<dependency>
    <groupId>me.ivovk</groupId>
    <artifactId>connect-rpc-java-netty</artifactId>
    <version>${connect-rpc-java.version}</version>
</dependency>
```

and for Gradle:

```
"me.ivovk:connect-rpc-java-netty:${connect-rpc-java.version}"
```

The entry point that allows the server to be started is `ConnectNettyServerBuilder`class:

```java
import me.ivovk.connect_rpc_java.netty.ConnectNettyServerBuilder;

// Your GRPC service(s)
List<io.grpc.ServiceDefinition> grpcServices = List.of(
    ExampleServiceGrpc.getServiceDefinition()
);

// Start the server
var server = ConnectNettyServerBuilder
    .forServices(grpcServices)
    .port(8080)
    .build();

// Stop the server
Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
```

## Examples

* Client-Server communication using Connect RPC protocol instead of GRPC: [link](https://github.com/igor-vovk/connect-rpc-java/tree/main/examples/client-server)

## Development

### Connect RPC

#### Running Connect-RPC conformance tests

Run the following command to run Connect-RPC conformance tests:

```shell
make test-conformance-stable
```

Execution results are output to STDOUT.
Diagnostic data from the server itself is written to the log file `out/out.log`.

#### Header Modifications

* All incoming `Connection-*` headers are removed, as they aren’t allowed by GRPC.
* All outgoing `grpc-*` headers are removed.
* Original `User-Agent` request header is renamed to `x-user-agent`,
  `user-agent` is set to the in-process client's User Agent (`grpc-java-inprocess/1.69.0`),
  there is no way to disable it.

## Links

* [Connect RPC website](https://connectrpc.com)
* [Connect RPC Java library](https://github.com/igor-vovk/connect-rpc-java/)
* [Connect RPC Scala library](https://github.com/igor-vovk/connect-rpc-scala)