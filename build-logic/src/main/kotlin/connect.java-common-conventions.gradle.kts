/*
 * Common conventions for Java projects in Connect RPC Java
 */

plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }

    // Explicitly set source and target compatibility for better Kotlin DSL compatibility
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    // Common test dependencies
    "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.1")
}
