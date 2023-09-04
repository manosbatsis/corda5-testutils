package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.github.manosbatsis.corda5.testutils.rest.client.loggerFor
import org.apache.commons.exec.*
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class GradleHelper(val projectDir: String) {
    companion object {
        private val logger = loggerFor(GradleHelper::class.java)
    }

    val gradleExecutable: String by lazy {
        if (System.getProperty("os.name").lowercase().contains("windows")) "gradlew.bat" else "./gradlew"
    }

    fun executeTask(vararg tasks: String) {
        logger.finer("Executing tasks: ${tasks.joinToString(",")}")
        executeTasks(tasks, false)
        logger.finer("Executed tasks: ${tasks.joinToString(",")}")
    }

    fun executeTaskAndWait(vararg tasks: String) {
        logger.finer("Executing tasks: ${tasks.joinToString(",")}")
        executeTasks(tasks, true)
        TimeUnit.SECONDS.sleep(4L)
        logger.info("Executed tasks: ${tasks.joinToString(",")}")
    }

    private fun executeTasks(tasks: Array<out String>, blocking: Boolean = false): ExecuteResultHandler? {
        val watchdog = ExecuteWatchdog(5 * 60 * 1000)
        val executor = DefaultExecutor()
            .also {
                it.workingDirectory = File(projectDir).absoluteFile
                it.setStreamHandler(PumpStreamHandler(System.out, System.err))
                it.watchdog = watchdog
            }
        // Don't mess with the JARs as they're already used by the test process
        val cmdline = CommandLine(gradleExecutable)
            .addArguments(tasks)
            .addArgument("-x")
            .addArgument("jar")
        return if (blocking) {
            executor.execute(cmdline)
            null
        } else {
            val resultHandler = GradleResultHandler(watchdog)
            executor.execute(cmdline, resultHandler)
            resultHandler
        }
    }

    private class GradleResultHandler : DefaultExecuteResultHandler {
        private var watchdog: ExecuteWatchdog? = null

        constructor(watchdog: ExecuteWatchdog?) {
            this.watchdog = watchdog
        }

        override fun onProcessComplete(exitValue: Int) {
            super.onProcessComplete(exitValue)
            logger.finer("Process completed successfully")
        }

        override fun onProcessFailed(e: ExecuteException) {
            super.onProcessFailed(e)
            if (watchdog != null && watchdog!!.killedProcess()) {
                logger.log(Level.SEVERE, "Process timed out")
            } else {
                logger.log(Level.SEVERE, "Process failed: " + e.message)
            }
        }
    }
}
