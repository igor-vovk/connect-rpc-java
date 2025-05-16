import gradle.kotlin.dsl.accessors._276ae2cd150398d14500ae0f74d8ebdf.mavenPublishing

/*
 * Common conventions for Java libraries in Connect RPC Java
 */

plugins {
    id("connect.java-common-conventions")
    id("connect.code-quality-conventions")
    `java-library`
    id("com.vanniktech.maven.publish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {

}