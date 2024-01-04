package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.manosbatsis.corda5.testutils.integration.junit5.CombinedWorkerMode
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.rest.client.*
import com.github.manosbatsis.corda5.testutils.integration.junit5.jackson.Corda5Module
import com.github.manosbatsis.corda5.testutils.rest.client.model.VirtualNodeInfo
import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import net.corda.v5.base.types.MemberX500Name
import java.util.concurrent.TimeUnit

class NodeHandlesHelper(
    val config: Corda5NodesConfig
) {

    companion object {
        private val logger = loggerFor(NodeHandlesHelper::class.java)

        var nodeHandlesCache: NodeHandles? = null
    }

    private val objectMapper by lazy {
        ObjectMapper().registerModules(JavaTimeModule(), kotlinModule(), Corda5Module())
            .also {
                if (config.objectMapperConfigurer != null) config.objectMapperConfigurer.invoke(it)
            }
    }

    val nodeHandles: NodeHandles by lazy {
        when (config.combinedWorkerMode) {
            CombinedWorkerMode.PER_CLASS ->
                reset().also { nodeHandlesCache = buildNodeHandles() }

            CombinedWorkerMode.SHARED ->
                if (nodeHandlesCache == null) nodeHandlesCache = buildNodeHandles()

            CombinedWorkerMode.NONE ->
                nodeHandlesCache = nodeHandles(nodesClient.nodes().virtualNodes)
        }
        nodeHandlesCache!!
    }

    private val gradle by lazy {
        GradleHelper(
            config.projectDir.absolutePath
        )
    }

    fun reset() {
        nodeHandlesCache = null
        stop()
    }

    fun stop() {
        gradle.executeTaskAndWait("stopCorda")
    }

    private fun buildNodeHandles(): NodeHandles {
        var nodesResponse: MutableList<VirtualNodeInfo>? =
            virtualNodeInfos(::nodesNullResponseCheck) {
                try {
                    gradle.executeTask("startCorda")
                } catch (e: Exception) {
                    // Force a (re)start even with a forgotten pid present
                    gradle.executeTaskAndWait("stopCorda")
                    gradle.executeTask("startCorda")
                }
            }

        logger.fine("Combined worker started, node list, size: ${nodesResponse!!.size}")
        if (nodesResponse.isEmpty()) {
            gradle.executeTaskAndWait("5-vNodeSetup")
            nodesResponse = virtualNodeInfos(::nodesEmptyResponseCheck)
        } else gradle.executeTaskAndWait("4-deployCPIs")

        return nodeHandles(nodesResponse!!)
    }

    private fun virtualNodeInfos(
        reloadCheck: (MutableList<VirtualNodeInfo>?) -> Boolean,
        onError: () -> Unit = {}
    ): MutableList<VirtualNodeInfo>? {
        var nodesResponse: MutableList<VirtualNodeInfo>? = null
        try {
            nodesResponse = nodesClient.nodes().virtualNodes
        } catch (e: Exception) {
            onError()
            var maxWait = 2 * 60
            logger.fine("Waiting for Combined Worker nodes...")
            while (reloadCheck(nodesResponse) && maxWait > 0) {
                maxWait -= 1
                TimeUnit.SECONDS.sleep(1L)
                try {
                    nodesResponse = nodesClient.nodes().virtualNodes
                } catch (e: Exception) {
                    logger.finer("Combined worker nodes not available ${e.message}")
                }
            }

            if (nodesNullResponseCheck(nodesResponse)) throw RuntimeException("Timed out while waiting for Combined Worker")
        }
        return nodesResponse
    }

    private fun nodesNullResponseCheck(nodesResponse: MutableList<VirtualNodeInfo>?) =
        nodesResponse == null

    private fun nodesEmptyResponseCheck(nodesResponse: MutableList<VirtualNodeInfo>?) =
        nodesResponse?.isEmpty() ?: true

    private fun nodeHandles(nodesResponse: MutableList<VirtualNodeInfo>): NodeHandles {
        val nodes = nodesResponse.map {
            NodeHandle(
                MemberX500Name.parse(it.holdingIdentity.x500Name),
                it.holdingIdentity.shortHash,
                flowsClient,
                objectMapper
            )
        }
        return NodeHandles(nodes)
    }

    private fun <F> buildFeignClient(
        cientType: Class<F>
    ): F = Feign.builder()
        .let {
            if (config.debug)
                it.logLevel(feign.Logger.Level.FULL).logger(CustomFeignRequestLogging())
            else it
        }
        .encoder(JacksonEncoder(objectMapper))
        .decoder(JacksonDecoder(objectMapper))
        .requestInterceptor(BasicAuthRequestInterceptor(config.authUsername, config.authPassword))
        .client(OkHttpClient(TrustAllCertsCOkHttpClient.trustAllCertsClient))
        .target(cientType, config.baseUrl)

    val flowsClient: FlowsClient by lazy {
        buildFeignClient(FlowsClient::class.java)
    }

    val nodesClient: VirtualNodesClient by lazy {
        buildFeignClient(VirtualNodesClient::class.java)
    }
}
