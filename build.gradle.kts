/*
 * Root project configuration for Connect RPC Java
 */

plugins {
    base
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" apply false
    // Apply any other common plugins here
}

allprojects {
    group = "me.ivovk"
    version = "0.1.0-SNAPSHOT"
}

tasks.wrapper {
    gradleVersion = "8.6"
    distributionType = Wrapper.DistributionType.ALL
}
