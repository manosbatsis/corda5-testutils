plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.7.0"
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinVersion = "1.7.0"//by System.getProperties()
val ktlintVersion = "10.3.0" //by System.getProperties()
val detektPlugin = "1.19.0"
val gradleNexusPublishPlugin = "1.1.0"
val gradleTestLoggerPlugin = "3.1.0"
val gradleVersionsPlugin = "0.39.0"
val kotlinDokkaPlugin = "1.7.0"
val kotlinxKoverPlugin = "0.5.0"
val useLatestVersionsPlugin = "0.2.18"
val springBootVersion = "2.7.4"
val springBootDependencyManagementVersion = "1.0.13.RELEASE"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("org.jetbrains.kotlin:kotlin-noarg")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:$ktlintVersion")

    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektPlugin")
    implementation("io.github.gradle-nexus:publish-plugin:$gradleNexusPublishPlugin")
    implementation("com.adarshr:gradle-test-logger-plugin:$gradleTestLoggerPlugin")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinDokkaPlugin")
    implementation("org.jetbrains.kotlinx:kover:$kotlinxKoverPlugin")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    implementation("io.spring.gradle:dependency-management-plugin:${springBootDependencyManagementVersion}")
}
