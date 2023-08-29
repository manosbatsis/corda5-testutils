package com.github.manosbatsis.corda5.testutils.integration.junit5.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HoldingIdentity(
    val x500Name: String,
    val shortHash: String,
)
