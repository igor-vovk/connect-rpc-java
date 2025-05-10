plugins {
    id("connect.library-conventions")
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
