package com.github.manosbatsis.corda5.testutils.rest.client

import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowRequest
import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowStatusResponse
import feign.Param
import feign.RequestLine

interface FlowsClient {
    @RequestLine("POST /flow/{holdingidentityshorthash}")
    fun flow(
        flowRequest: FlowRequest,
        @Param("holdingidentityshorthash") shortHash: String
    ): FlowStatusResponse

    @RequestLine("GET /flow/{holdingidentityshorthash}/{clientrequestid}")
    fun flowStatus(
        @Param("holdingidentityshorthash") shortHash: String,
        @Param("clientrequestid") requestId: String
    ): FlowStatusResponse
}
