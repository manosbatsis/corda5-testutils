package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.manosbatsis.corda5.testutils.integration.junit5.Corda5NodesConfig
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.CustomFeignRequestLogging
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.FlowsClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.TrustAllCertsCOkHttpClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.VirtualNodesClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.VirtualNodeInfo
import feign.Feign
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import net.corda.v5.base.types.MemberX500Name
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File
import java.util.concurrent.TimeUnit


class GradleHelper(gradleInstallationDir: String, projectDir: String) {
    private val connector: GradleConnector

    init {
        connector = GradleConnector.newConnector()
        connector.useInstallation(File(gradleInstallationDir))
        connector.forProjectDirectory(File(projectDir))
    }

    fun executeTask(vararg tasks: String) {
        val connection: ProjectConnection = connector.connect()
        val build: BuildLauncher = connection.newBuild()
        build.forTasks(*tasks)
        build.run()
        connection.close()
    }

}


class NodeHandlesHelper(
    val config: Corda5NodesConfig
) {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())
    private val toolingAPI by lazy {
        GradleHelper(
            config.gradleInstallationDir.absolutePath,
            config.projectDir.absolutePath)
    }

    var nodesResponse: MutableList<VirtualNodeInfo>? = null
    private fun buildNodeHandles(): NodeHandles {
        if(nodesResponse == null) try{
            nodesResponse = nodesClient.nodes().virtualNodes
        }catch (e: Exception){
            toolingAPI.executeTask("startCorda")
            var maxWait = 2*60
            while (nodesResponse == null && maxWait > 0){
                maxWait = maxWait - 1
                TimeUnit.SECONDS.sleep(1L)
                nodesResponse = nodesClient.nodes().virtualNodes
            }

            if(nodesResponse == null) throw RuntimeException("Starting Corda timed out")
        }

        if(nodesResponse!!.isEmpty()) toolingAPI.executeTask("5-vNodeSetup")
        else toolingAPI.executeTask("4-deployCPIs")
        val nodes = nodesResponse!!.map {
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
