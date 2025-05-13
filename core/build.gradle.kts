import com.google.protobuf.gradle.*

plugins {
    id("connect.library-conventions")
    id("connect.protobuf-conventions")
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
    implementation(libs.logback)
    implementation(libs.protobuf.util)
    testImplementation(libs.grpc.stub)
    testImplementation(libs.grpc.common.protos)
    testImplementation(libs.javax.annotation)
}
