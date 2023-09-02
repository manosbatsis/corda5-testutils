// A settings.gradle.kts plugin for defining shared repositories used by both buildSrc and the root project

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            mavenContent { snapshotsOnly() }
        }
    }

    pluginManagement {
        repositories {
            gradlePluginPortal()
            mavenCentral()
            maven { url = uri("(https://repo.gradle.org/gradle/libs-releases/") }

        }
    }
}
