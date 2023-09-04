package com.github.manosbatsis.corda5.testutils.rest.client

import com.github.manosbatsis.corda5.testutils.rest.client.model.VirtualNodes
import feign.RequestLine

interface VirtualNodesClient {
    @RequestLine("GET /virtualnode")
    fun nodes(): VirtualNodes
}
