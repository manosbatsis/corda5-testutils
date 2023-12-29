package com.github.manosbatsis.corda5.testutils.rest.client

import java.util.logging.Logger
import kotlin.reflect.full.companionObject

fun loggerFor(forClass: Class<*>): Logger =
    Logger.getLogger(
        forClass.enclosingClass?.takeIf {
            it.kotlin.companionObject?.java == forClass
        }?.canonicalName ?: forClass.canonicalName
    )

fun Any.objectLogger(): Logger = Logger.getLogger(javaClass.enclosingClass.canonicalName)
