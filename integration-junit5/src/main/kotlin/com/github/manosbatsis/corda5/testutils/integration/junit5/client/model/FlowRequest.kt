package com.github.manosbatsis.corda5.testutils.integration.junit5.client.model

import java.util.*

data class FlowRequest(
    val clientRequestId: String = UUID.randomUUID().toString(),
    val flowClassName: String,
    val requestBody: Any?
)
