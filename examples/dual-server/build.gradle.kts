import com.google.protobuf.gradle.id

plugins {
    id("connect.java-common-conventions")
    id("connect.code-quality-conventions")
    id("connect.protobuf-conventions")
    application
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

dependencies {
    implementation(project(":core"))
    implementation(project(":netty"))
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty)
    implementation(libs.logback)
    implementation(libs.javax.annotation)
}

application {
    mainClass.set("me.ivovk.connect_rpc_java.examples.dual_server.Main")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}
