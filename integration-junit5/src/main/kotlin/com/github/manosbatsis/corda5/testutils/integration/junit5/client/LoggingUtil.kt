package com.github.manosbatsis.corda5.testutils.integration.junit5.client

import kotlin.reflect.full.companionObject
import java.util.logging.Logger

fun loggerFor(forClass: Class<*>): Logger =
    Logger.getLogger(
        forClass.enclosingClass?.takeIf {
            it.kotlin.companionObject?.java == forClass
        }?.canonicalName ?: forClass.canonicalName
    )

fun Any.objectLogger(): Logger = Logger.getLogger(javaClass.enclosingClass.canonicalName)
