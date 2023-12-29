package com.github.manosbatsis.corda5.testutils.rest.client

import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowRequest
import com.github.manosbatsis.corda5.testutils.rest.client.model.FlowStatusResponse
import feign.Param
import feign.RequestLine

interface FlowsClient {
    @RequestLine("POST /flow/{holdingidentityshorthash}")
    fun <T> flow(
        flowRequest: FlowRequest<T>,
        @Param("holdingidentityshorthash") shortHash: String
    ): FlowStatusResponse<T>

    @RequestLine("GET /flow/{holdingidentityshorthash}/{clientrequestid}")
    fun <T> flowStatus(
        @Param("holdingidentityshorthash") shortHash: String,
        @Param("clientrequestid") requestId: String
    ): FlowStatusResponse<T>
}
