package com.github.manosbatsis.corda5.testutils.integration.junit5

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace

/**
 * Base class for extensions that wish to provide a Corda network
 * throughout test suite execution
 */
abstract class AbstractCorda5Extension : BeforeAllCallback, AfterAllCallback, JupiterExtensionConfigSupport {

    lateinit var config: Corda5NodesConfig
    protected var started = false

    abstract fun getNamespace(): Namespace

    abstract fun getConfig(
        extensionContext: ExtensionContext
    ): Corda5NodesConfig

    /** Start the Corda network */
    override fun beforeAll(extensionContext: ExtensionContext) {
        config = getConfig(extensionContext)
        started = true
    }

    /** Stop the Corda network */
    override fun afterAll(extensionContext: ExtensionContext) {
        // NO-OP
    }
}
