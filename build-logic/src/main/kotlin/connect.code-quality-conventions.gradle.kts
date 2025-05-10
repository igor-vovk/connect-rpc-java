/*
 * Code formatting and quality conventions for Connect RPC Java
 */

plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        // Set specific formatting options
        googleJavaFormat()
        removeUnusedImports()
        importOrder("", "java")

        // Enforce license header
        //licenseHeaderFile(rootProject.file("gradle/spotless/license-header.txt"))

        // Exclude generated files from formatting
        targetExclude("**/build/generated/**")
    }
}
