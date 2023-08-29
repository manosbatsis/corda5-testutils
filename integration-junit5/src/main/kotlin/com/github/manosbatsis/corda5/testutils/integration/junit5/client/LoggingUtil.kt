package com.github.manosbatsis.corda5.testutils.integration.junit5.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

fun loggerFor(forClass: Class<*>): Logger =
    LoggerFactory.getLogger(
        forClass.enclosingClass?.takeIf {
            it.kotlin.companionObject?.java == forClass
        } ?: forClass
    )

fun Any.objectLogger(): Logger = LoggerFactory.getLogger(javaClass.enclosingClass)
