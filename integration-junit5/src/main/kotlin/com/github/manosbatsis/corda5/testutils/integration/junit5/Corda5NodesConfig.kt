package com.github.manosbatsis.corda5.testutils.integration.junit5

import java.io.File

fun gradleRootDir(){
    var currentDir = File("user.dir")
    while(File(currentDir.parentFile, "build.gradle").exists()
        || File(currentDir.parentFile, "build.gradle.kts").exists() ){
        currentDir = currentDir.parentFile
    }
}

enum class CombinedWorkerMode{
    /**
     * Default. Will launch, setup VNodes and (re)deploy to the Combined Worker
     * as needed only once and reuse it for all tests.
     */
    SHARED,
    /**
     * Will launch, setup VNodes and (re)deploy to the Combined Worker
     * as needed for each individual test class.
     */
    PER_CLASS,
    /**
     * Completely disables automation for the Combined Worker to enable manual or external management.
     */
    NONE
}
data class Corda5NodesConfig(
    val authUsername: String = "admin",
    val authPassword: String = "admin",
    val baseUrl: String = "https://localhost:8888/api/v1/",
    val httpMaxWaitSeconds: Int = 60,
    val debug: Boolean = false,
    val gradleInstallationDir: File = gradleRootDir,
    val projectDir: File = gradleRootDir,
    val combinedWorkerMode: CombinedWorkerMode = CombinedWorkerMode.SHARED
){
    companion object{
        val gradleRootDir: File by lazy {
            var currentDir = File("user.dir")
            while(File(currentDir.parentFile, "build.gradle").exists()
                || File(currentDir.parentFile, "build.gradle.kts").exists() ){
                currentDir = currentDir.parentFile
            }
            currentDir
        }
    }
}
