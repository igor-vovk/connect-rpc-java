import com.google.protobuf.gradle.*

plugins {
    id("connect.library-conventions")
    id("connect.protobuf-conventions")
}

protobuf {
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

dependencies {
    api(project(":netty"))
    implementation(libs.grpc.core)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.slf4j.api)
    implementation(libs.javax.annotation)
}
