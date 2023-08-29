// Root project name, used in naming the project as a whole and used in naming objects built by the project.
rootProject.name = "corda5-testutils"
include(":integration-junit5")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
apply(from = "./buildSrc/repositories.settings.gradle.kts")

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}
