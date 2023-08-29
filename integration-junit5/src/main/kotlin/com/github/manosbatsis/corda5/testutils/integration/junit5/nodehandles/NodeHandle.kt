package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.FlowRequest
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.FlowStatusResponse
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.FlowsClient
import com.github.manosbatsis.corda5.testutils.integration.junit5.client.loggerFor
import net.corda.v5.base.types.MemberX500Name
import java.util.concurrent.TimeUnit

data class NodeHandle(
    val memberX500Name: MemberX500Name,
    val holdingIdentityShortHash: String,
    var flowsClient: FlowsClient
) {

    companion object {
        private val logger = loggerFor(NodeHandle::class.java)
    }

    fun waitForFlow(flowRequest: FlowRequest, maxWaitSec: Int = 60): FlowStatusResponse {
        val clientRequestId: String = flowRequest.clientRequestId
        var flowStatusResponse: FlowStatusResponse = flowsClient.flow(flowRequest, holdingIdentityShortHash)
        for (i in 0..maxWaitSec) {
            TimeUnit.SECONDS.sleep(1)
            flowStatusResponse = flowsClient.flowStatus(holdingIdentityShortHash, clientRequestId)
            when {
                flowStatusResponse.isFinal() -> break
                else -> logger.debug("Non-final flow status will retry $flowStatusResponse")
            }
        }
        return flowStatusResponse
    }
}
