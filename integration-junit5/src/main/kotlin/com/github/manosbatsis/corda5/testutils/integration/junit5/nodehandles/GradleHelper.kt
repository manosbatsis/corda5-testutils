package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.github.manosbatsis.corda5.testutils.integration.junit5.client.loggerFor
import org.apache.commons.exec.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class GradleHelper(val projectDir: String) {
    companion object {
        private val logger = loggerFor(GradleHelper::class.java)
    }

    fun executeTask(vararg tasks: String) {
        logger.info("executing tasks: ${tasks.joinToString(",")}")
        try {
            executeTasks(tasks, false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        logger.info("executed tasks: ${tasks.joinToString(",")}")
    }

    fun executeTaskAndWait(vararg tasks: String) {
        logger.info("executing tasks: ${tasks.joinToString(",")}")
        try {
            executeTasks(tasks, true)
            TimeUnit.SECONDS.sleep(4L)
        } catch (e: IOException) {
            logger.severe("Failed tasks, error: ${e.message}")
            e.printStackTrace()
        }
        logger.info("executed tasks: ${tasks.joinToString(",")}")
    }

    private fun executeTasks(tasks: Array<out String>, blocking: Boolean = false): ExecuteResultHandler? {
        val watchdog = ExecuteWatchdog(5 * 60 * 1000)
        val executor = DefaultExecutor()
            .also {
                it.workingDirectory = File(projectDir).absoluteFile
                it.setStreamHandler(PumpStreamHandler(System.out, System.err))
                it.watchdog = watchdog
            }

        val cmdline = CommandLine(gradleExecutable())
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

    private fun gradleExecutable(): String = "./gradlew"

    private class GradleResultHandler : DefaultExecuteResultHandler {
        private var watchdog: ExecuteWatchdog? = null

        constructor(watchdog: ExecuteWatchdog?) {
            this.watchdog = watchdog
        }

        constructor(exitValue: Int) {
            super.onProcessComplete(exitValue)
        }

        override fun onProcessComplete(exitValue: Int) {
            super.onProcessComplete(exitValue)
            logger.info("[resultHandler] The document was successfully printed ...")
        }

        override fun onProcessFailed(e: ExecuteException) {
            super.onProcessFailed(e)
            if (watchdog != null && watchdog!!.killedProcess()) {
                System.err.println("[resultHandler] The print process timed out")
            } else {
                System.err.println("[resultHandler] The print process failed to do : " + e.message)
            }
        }
    }
}
