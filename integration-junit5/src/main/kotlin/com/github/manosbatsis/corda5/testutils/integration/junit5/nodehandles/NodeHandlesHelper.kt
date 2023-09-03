package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.manosbatsis.corda5.testutils.integration.junit5.CombinedWorkerMode
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.*
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.VirtualNodeInfo
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
        private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())

        var nodeHandlesCache: NodeHandles? = null
    }

    val nodeHandles: NodeHandles
        get() {
            when (config.combinedWorkerMode) {
                CombinedWorkerMode.PER_CLASS ->
                    reset().also { nodeHandlesCache = buildNodeHandles() }

                CombinedWorkerMode.SHARED ->
                    if (nodeHandlesCache == null) nodeHandlesCache = buildNodeHandles()

                CombinedWorkerMode.NONE ->
                    nodeHandlesCache = nodeHandles(nodesClient.nodes().virtualNodes)
            }
            return nodeHandlesCache!!
        }

    private val toolingAPI by lazy {
        GradleHelper(
            config.projectDir.absolutePath
        )
    }

    fun reset() {
        nodeHandlesCache = null
        toolingAPI.executeTaskAndWait("stopCorda")
    }

    private fun buildNodeHandles(): NodeHandles {
        var nodesResponse: MutableList<VirtualNodeInfo>? =
            virtualNodeInfos(::nodesNullResponseCheck) {
                toolingAPI.executeTask("startCorda")
            }

        logger.info("Combined worker started, node list, size: ${nodesResponse!!.size}")
        if (nodesResponse.isEmpty()) {
            toolingAPI.executeTaskAndWait("5-vNodeSetup")
            nodesResponse = virtualNodeInfos(::nodesEmptyResponseCheck)
        } else toolingAPI.executeTaskAndWait("4-deployCPIs")

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
            while (reloadCheck(nodesResponse) && maxWait > 0) {
                maxWait -= 1
                TimeUnit.SECONDS.sleep(1L)

                logger.info("Waiting for Combined Worker nodes: $maxWait")
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
                flowsClient
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
