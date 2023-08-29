package com.github.manosbatsis.corda5.testutils.integration.junit5.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VirtualNodeInfo(
    val holdingIdentity: HoldingIdentity
)
