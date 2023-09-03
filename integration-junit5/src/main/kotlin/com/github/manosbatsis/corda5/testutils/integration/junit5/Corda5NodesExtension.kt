package com.github.manosbatsis.corda5.testutils.integration.junit5

import com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles.NodeHandles
import com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles.NodeHandlesHelper
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class Corda5NodesExtension : AbstractCorda5Extension(), ParameterResolver {

    companion object {
        private val namespace: Namespace = Namespace.create(Corda5NodesExtension::class)
    }

    private var nodeHandlesHelper: NodeHandlesHelper? = null

    override fun beforeAll(extensionContext: ExtensionContext) {
        super.beforeAll(extensionContext)
        nodeHandlesHelper = NodeHandlesHelper(config)
    }

    override fun afterAll(extensionContext: ExtensionContext) {
        super.afterAll(extensionContext)
    }

    override fun getConfig(
        extensionContext: ExtensionContext
    ) = findConfig(getRequiredTestClass(extensionContext))

    override fun clearNodeHandles() {
        nodeHandlesHelper?.reset()
    }

    fun findConfig(testClass: Class<*>): Corda5NodesConfig =
        findFieldValue(testClass, Corda5NodesConfig::class.java)
            ?: Corda5NodesConfig()

    override fun getNamespace(): Namespace = namespace

    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ) =
        parameterContext?.parameter?.type == NodeHandles::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ) = nodeHandlesHelper!!.nodeHandles
}
