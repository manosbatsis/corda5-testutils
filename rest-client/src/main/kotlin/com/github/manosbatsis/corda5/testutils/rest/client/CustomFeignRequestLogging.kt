package com.github.manosbatsis.corda5.testutils.rest.client

import feign.Logger
import feign.Request
import feign.Response
import java.io.IOException

class CustomFeignRequestLogging : Logger() {
    companion object {

        private val logger = loggerFor(CustomFeignRequestLogging::class.java)
    }

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        if (logLevel.ordinal >= Level.HEADERS.ordinal) {
            super.logRequest(configKey, logLevel, request)
        } else {
            var bodyLength = 0
            if (request.body() != null) {
                bodyLength = request.body().size
            }
            log(configKey, "---> %s %s HTTP/1.1 (%s-byte body) ", request.httpMethod().name, request.url(), bodyLength)
        }
    }

    @Throws(IOException::class)
    override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        return if (logLevel.ordinal >= Level.HEADERS.ordinal) {
            super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime)
        } else {
            val status = response.status()
            val request = response.request()
            log(
                configKey,
                "<--- %s %s HTTP/1.1 %s (%sms) ",
                request.httpMethod().name,
                request.url(),
                status,
                elapsedTime
            )
            response
        }
    }

    override fun log(configKey: String, format: String, vararg args: Any) {
        logger.info(format(configKey, format, *args))
    }

    protected fun format(configKey: String?, format: String, vararg args: Any?): String {
        return String.format(methodTag(configKey) + format, *args)
    }
}
