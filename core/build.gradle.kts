plugins {
    `java-library`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(libs.grpc.core)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.inprocess)
    implementation(libs.logback)
    implementation(libs.protobuf.util)
    testImplementation(libs.junit)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
