package com.github.manosbatsis.corda5.testutils.integration.junit5.client

import com.github.manosbatsis.corda5.testutils.integration.junit5.client.model.VirtualNodes
import feign.RequestLine

interface VirtualNodesClient {
    @RequestLine("GET /virtualnode")
    fun nodes(): VirtualNodes
}
