package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.manosbatsis.corda5.testutils.rest.client.FlowsClient
import com.github.manosbatsis.corda5.testutils.rest.client.loggerFor
import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowRequest
import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowStatusResponse
import net.corda.v5.base.types.MemberX500Name
import java.util.concurrent.TimeUnit

data class NodeHandle(
    val memberX500Name: MemberX500Name,
    val holdingIdentityShortHash: String,
    var flowsClient: FlowsClient,
    val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = loggerFor(NodeHandle::class.java)
    }

    fun <T> waitForFlow(
        flowRequest: FlowRequest<T>,
        maxWaitSec: Int = 60
    ): FlowStatusResponse<T> {
        val clientRequestId: String = flowRequest.clientRequestId
        var flowStatusResponse = flowsClient.flow(
            // OpenFeign won't do generics, switch to string temporarily
            flowRequest.withFlowResultClass(String::class.java),
            holdingIdentityShortHash
        )
        for (i in 0..maxWaitSec) {
            TimeUnit.SECONDS.sleep(1)
            flowStatusResponse = flowsClient.flowStatus(holdingIdentityShortHash, clientRequestId)
            when {
                flowStatusResponse.isFinal() -> break
                else -> logger.info("Non-final flow status will retry $flowStatusResponse")
            }
        }

        return flowStatusResponse.withFlowResult(
            flowStatusResponse.flowResult?.let {
                objectMapper.readValue(it, flowRequest.flowResultClass)
            }
        )
    }
}
