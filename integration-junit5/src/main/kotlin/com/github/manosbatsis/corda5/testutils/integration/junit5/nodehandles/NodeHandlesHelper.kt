package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.CustomFeignRequestLogging
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.FlowsClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.TrustAllCertsCOkHttpClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.VirtualNodesClient
import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import net.corda.v5.base.types.MemberX500Name

class NodeHandlesHelper(
    val config: Corda5NodesConfig
) {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())

    private fun buildNodeHandles(): NodeHandles {
        val nodesResponse = nodesClient.nodes().virtualNodes
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

    val nodeHandles by lazy { buildNodeHandles() }
}
