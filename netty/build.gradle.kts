plugins {
    id("connect.library-conventions")
}

mavenPublishing {
    coordinates(artifactId = "connect-rpc-java-netty")
    pom {
        name.set("Connect RPC Java Netty")
        description.set("Netty implementation for Connect RPC Java")
    }
}

dependencies {
    api(project(":core"))
    implementation(libs.grpc.core)
    implementation(libs.grpc.netty)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.slf4j.api)
    api(libs.netty.all)
}
