package com.github.manosbatsis.corda5.testutils.integration.junit5

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import org.junit.jupiter.api.parallel.ResourceLock

/**
 * Base class for extensions that wish to provide a Corda network
 * throughout test suite execution
 */
abstract class AbstractCorda5Extension : BeforeAllCallback, AfterAllCallback, JupiterExtensionConfigSupport {

    companion object {
        private const val allCallbackCounterKey = "AbstractCorda5Extension#allCallbackCounterKey"
    }

    lateinit var config: Corda5NodesConfig

    abstract fun getNamespace(): Namespace

    abstract fun getConfig(
        extensionContext: ExtensionContext
    ): Corda5NodesConfig

    /** Start the Corda network */
    @ResourceLock(allCallbackCounterKey)
    override fun beforeAll(extensionContext: ExtensionContext) {
        config = getConfig(extensionContext)
        if (config.combinedWorkerEnabled) {
            val incrementedCallbackCount = extensionContext.root.getStore(GLOBAL).getOrDefault(allCallbackCounterKey, Int::class.java, 0)
                .plus(1)
            extensionContext.root.getStore(GLOBAL).put(allCallbackCounterKey, incrementedCallbackCount)
            if (incrementedCallbackCount == 1 && config.combinedWorkerMode != CombinedWorkerMode.SHARED) clearNodeHandles()
            initNodeHandles()
        }
    }

    /** Stop the Corda network */
    @ResourceLock(allCallbackCounterKey)
    override fun afterAll(extensionContext: ExtensionContext) {
        if (config.combinedWorkerEnabled) {
            val decrementedCallbackCount = extensionContext.root.getStore(GLOBAL).get(allCallbackCounterKey, Int::class.java)
                .minus(1)
            extensionContext.root.getStore(GLOBAL).put(allCallbackCounterKey, decrementedCallbackCount)
            val launcherFinished = decrementedCallbackCount == 0 && config.combinedWorkerMode == CombinedWorkerMode.PER_LAUNCHER
            if (launcherFinished || config.combinedWorkerMode == CombinedWorkerMode.PER_CLASS)
                clearNodeHandles()
        }
    }

    abstract fun initNodeHandles()

    abstract fun clearNodeHandles()

    abstract fun stopNodesNetwork()
}
