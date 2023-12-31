package com.github.manosbatsis.corda5.testutils.integration.junit5

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.manosbatsis.corda5.testutils.rest.client.loggerFor
import java.io.File

fun gradleRootDir() {
    var currentDir = File("user.dir")
    while (File(currentDir.parentFile, "build.gradle").exists() || File(
            currentDir.parentFile,
            "build.gradle.kts"
        ).exists()
    ) {
        currentDir = currentDir.parentFile
    }
}

enum class CombinedWorkerMode {
    /**
     * Default. Will start, setup VNodes and (re)deploy to the Combined Worker
     * as needed only once for all tests. Leaves the worker running.
     */
    SHARED,

    /**
     * Will re-launch, setup VNodes and (re)deploy to the Combined Worker
     * as needed for each individual test class.
     */
    PER_CLASS,

    /**
     * Will re-launch, setup VNodes and (re)deploy to the Combined Worker
     * as needed for every JUnit LauncherSession. Stops the worker on finish.
     */
    PER_LAUNCHER,

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
    val projectDir: File = gradleRootDir,
    val combinedWorkerMode: CombinedWorkerMode = CombinedWorkerMode.SHARED,
    val objectMapperConfigurer: ((ObjectMapper) -> Unit)? = null
) {
    companion object {
        private val logger = loggerFor(Corda5NodesConfig::class.java)
        val gradleRootDir: File by lazy {
            var currentDir = File(System.getProperty("user.dir"))
            while (File(currentDir.parentFile, "build.gradle").exists() || File(
                    currentDir.parentFile,
                    "build.gradle.kts"
                ).exists()
            ) {
                currentDir = currentDir.parentFile
            }
            logger.fine("Using Gradle module dir: ${currentDir.absolutePath}")
            currentDir
        }
    }

    val combinedWorkerEnabled = combinedWorkerMode != CombinedWorkerMode.NONE
}
