/*
 * Root project configuration for Connect RPC Java
 */

plugins {
    base
    idea
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" apply false
    // Apply any other common plugins here
}

fun getVersionFromFile(): String {
    val versionFromFile = project.file("version.properties")

    return if (versionFromFile.exists()) {
        val props = java.util.Properties()
        versionFromFile.inputStream().use(props::load)
        props.getProperty("version")
    } else {
        "0.1.0-SNAPSHOT"
    }
}

allprojects {
    version = getVersionFromFile()
}
