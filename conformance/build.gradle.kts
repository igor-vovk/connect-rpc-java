import com.google.protobuf.gradle.*

plugins {
    application
    id("connect.library-conventions")
    id("connect.protobuf-conventions")
}

protobuf {
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") {}
            }
        }
    }
}

application {
    version = "1.0.0"
    mainClass = "me.ivovk.connect_rpc_java.conformance.ServerLauncher"
}

dependencies {
    api(project(":netty"))
    implementation(libs.grpc.core)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.slf4j.api)
    implementation(libs.javax.annotation)
}
