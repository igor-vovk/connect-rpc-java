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
    api(project(":core"))
    implementation(libs.grpc.core)
    implementation(libs.slf4j.api)
    api(libs.netty.all)

    testImplementation(libs.junit)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
