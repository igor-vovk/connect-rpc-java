import com.google.protobuf.gradle.*

plugins {
    id("connect.library-conventions")
    id("connect.protobuf-conventions")
}

mavenPublishing {
    coordinates(artifactId = "connect-rpc-java-core")
    pom {
        name.set("Connect RPC Java Core")
        description.set("Core library for Connect RPC Java")
    }
}

protobuf {
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

dependencies {
    implementation(libs.grpc.core)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.inprocess)
    implementation(libs.protobuf.java)
    implementation(libs.protobuf.util)
    implementation(libs.gson)
    implementation(libs.slf4j.api)
    testImplementation(libs.grpc.stub)
    testImplementation(libs.grpc.common.protos)
    testImplementation(libs.javax.annotation)
}
