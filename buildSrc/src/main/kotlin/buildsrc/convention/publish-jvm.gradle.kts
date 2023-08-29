package buildsrc.convention

import buildsrc.config.createManosBatsisPom

plugins {
    `maven-publish`
    signing
}

description = "Configuration for publishing Jvm libraries to Sonatype Maven Central"

val signingKeyId: String? by project
val signingKey: String? by project
val signingPassword: String? by project

val signingEnabled: Provider<Boolean> = provider {
    signingKeyId != null && signingKey != null && signingPassword != null
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    // Gradle warns about some signing tasks using publishing task outputs without explicit dependencies
    dependsOn(tasks.withType<Sign>())
}
// Github Packages:
// do NOT publish from your developer host!
// to release: 1. remove SNAPSHOT from version; 2. commit & push; 3. check github workflow results
// if the workflow tries to publish the same release again you'll get: "Received status code 409 from server: Conflict"
// Maven Central:
// https://central.sonatype.org/publish/release/
val repositoryVendor = System.getProperty("publishTargetRepo", "local")
publishing {
    repositories {
        when (repositoryVendor) {
            "local" -> {
                println("> Publish to Maven repo: local")
                // publish to local repo, for testing
                mavenLocal()
            }
            "github" -> {
                println("> Publish to Maven repo: github")
                maven {
                    name = "GitHubPackages" // Must match regex [A-Za-z0-9_\-.]+.
                    url = uri("https://maven.pkg.github.com/manosbatsis/${rootProject.name}")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
            "sonatype" -> {
                maven {
                    name = "Sonatype"
                    val host = "https://s01.oss.sonatype.org"
                    val path = if (version.toString().endsWith("SNAPSHOT")) "/content/repositories/snapshots/"
                    else "/service/local/staging/deploy/maven2/"
                    url = uri(host.plus(path))
                    println("> Publish to $url")
                    credentials {
                        username = project.findProperty("ossrh.username") as String?
                        password = project.findProperty("ossrh.password") as String?
                    }
                }
            }
        }
    }
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])
        suppressAllPomMetadataWarnings()
        createManosBatsisPom("${project.name}", "${project.description}")
    }
}

signing {
    if (signingEnabled.get()) {
        sign(publishing.publications["mavenJava"])

        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
}
