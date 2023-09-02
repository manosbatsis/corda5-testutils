package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File

class GradleHelper(gradleInstallationDir: String, projectDir: String) {
    private val connector: GradleConnector

    init {
        connector = GradleConnector.newConnector()
        connector.useInstallation(File(gradleInstallationDir))
        connector.forProjectDirectory(File(projectDir))
    }

    fun executeTask(vararg tasks: String) {
        val connection: ProjectConnection = connector.connect()
        val build: BuildLauncher = connection.newBuild()
        build.forTasks(*tasks)
        build.run()
        connection.close()
    }

}