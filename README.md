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

In addition, the library allows creating free-form REST APIs
using [GRPC Transcoding](https://cloud.google.com/endpoints/docs/grpc/transcoding) approach (based on `google.api.http`
annotations that can be added to methods):

```protobuf
syntax = "proto3";

package example;

import "google/api/annotations.proto";

service ExampleService {
  rpc GetExample(GetExampleRequest) returns (GetExampleResponse) {
    option (google.api.http) = {
      get: "/example/{id}"
    };
  }
}

message GetExampleRequest {
  string id = 1;
}

message GetExampleResponse {
  string name = 1;
}
```

In addition to the previous way of execution, such endpoints are exposed in a more RESTful way:

```http
GET /example/123 HTTP/1.1

HTTP/1.1 200 OK

{
  "name": "example"
}
```

---
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

The entry point that allows the server to be started is `NettyServerBuilder`class:

```java
import me.ivovk.connect_rpc_java.netty.NettyServerBuilder;

// Your GRPC service(s)
List<io.grpc.ServiceDefinition> grpcServices = List.of(
    ExampleServiceGrpc.getServiceDefinition()
);

// Start the server
NettyServer server = NettyServerBuilder
        .forServices(grpcServices)
        .port(8080)
        .build();

// Stop the server
Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
```

## Development

### Connect RPC

#### Running Connect-RPC conformance tests

Run the following command to run Connect-RPC conformance tests:

```shell
docker build -f conformance-build/Dockerfile . --progress=plain --output "out" --build-arg config=suite-netty.yaml
```

Execution results are output to STDOUT.
Diagnostic data from the server itself is written to the log file `out/out.log`.

#### Header Modifications

* All incoming `Connection-*` headers are removed, as they aren’t allowed by GRPC.
* All outgoing `grpc-*` headers are removed.
* Original `User-Agent` request header is renamed to `x-user-agent`,
  `user-agent` is set to the in-process client's User Agent (`grpc-java-inprocess/1.69.0`),
  there is no way to disable it.
