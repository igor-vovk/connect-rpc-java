import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

/*
 * Common conventions for Java libraries in Connect RPC Java
 */

plugins {
    id("connect.java-common-conventions")
    id("connect.code-quality-conventions")
    id("com.vanniktech.maven.publish")
    `java-library`
}

mavenPublishing {
    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true,
        )
    )

    publishToMavenCentral()

    signAllPublications()

    coordinates(groupId = "me.ivovk")

    pom {
        name.set("Connect RPC Java Library")
        description.set("Connect RPC Java library")
        inceptionYear.set("2025")
        url.set("https://github.com/igor-vovk/connect-rpc-java")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("igor-vovk")
                name.set("Ihor Vovk")
                url.set("https://github.com/igor-vovk")
            }
        }
        scm {
            url.set("https://github.com/igor-vovk/connect-rpc-java")
            connection.set("scm:git:git://github.com/igor-vovk/connect-rpc-java.git")
            developerConnection.set("scm:git:ssh://git@github.com/igor-vovk/connect-rpc-java.git")
        }
    }
}