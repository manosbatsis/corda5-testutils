package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
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

    companion object{
        private val logger = loggerFor(NodeHandlesHelper::class.java)
        private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())

        var nodeHandlesCache: NodeHandles? = null
    }
    val nodeHandles: NodeHandles
        get() {
            if(nodeHandlesCache == null) nodeHandlesCache = buildNodeHandles()
            return nodeHandlesCache!!
        }

    private val toolingAPI by lazy {
        GradleHelper(
            config.gradleInstallationDir.absolutePath,
            config.projectDir.absolutePath)
    }

    fun reset(){
        nodeHandlesCache = null
    }

    private fun buildNodeHandles(): NodeHandles {

        var nodesResponse: MutableList<VirtualNodeInfo>? = null
        try{
            nodesResponse = nodesClient.nodes().virtualNodes
        }catch (e: Exception){
            toolingAPI.executeTask("startCorda")
            var maxWait = 2*60
            while (nodesResponse == null && maxWait > 0){
                maxWait = maxWait - 1
                TimeUnit.SECONDS.sleep(1L)
                try {
                    nodesResponse = nodesClient.nodes().virtualNodes
                }catch (e: Exception){
                    logger.debug("Waiting for Combined Worker...")
                }
            }

            if(nodesResponse == null) throw RuntimeException("Timed out while waiting for Combined Worker")
        }

        if(nodesResponse!!.isEmpty()) toolingAPI.executeTask("5-vNodeSetup")
        else toolingAPI.executeTask("4-deployCPIs")

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
