/*
 * Common conventions for Java libraries in Connect RPC Java
 */

plugins {
    id("connect.java-common-conventions")
    id("connect.code-quality-conventions")
    `java-library`
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Connect RPC implementation for Java - ${project.name}")
                url.set("https://github.com/igor-vovk/connect-rpc-java")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("igor-vovk")
                        name.set("Ihor Vovk")
                        email.set("")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/igor-vovk/connect-rpc-java.git")
                    developerConnection.set("scm:git:ssh://github.com/igor-vovk/connect-rpc-java.git")
                    url.set("https://github.com/igor-vovk/connect-rpc-java")
                }
            }
        }
    }
}
