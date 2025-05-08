/*
 * Convention for projects that use Protobuf
 */

import com.google.protobuf.gradle.*

plugins {
    id("connect.java-common-conventions")
    id("com.google.protobuf")
}

configure<ProtobufExtension> {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }

    plugins {
        register("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.72.0"
        }
    }
}
