package com.github.manosbatsis.corda5.testutils.rest.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VirtualNodeInfo(
    val holdingIdentity: HoldingIdentity
)
