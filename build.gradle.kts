/*
 * Root project configuration for Connect RPC Java
 */

plugins {
    base
    idea
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" apply false
    // Apply any other common plugins here
}

allprojects {
    version = "0.1.0"
}

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = Wrapper.DistributionType.ALL
}
