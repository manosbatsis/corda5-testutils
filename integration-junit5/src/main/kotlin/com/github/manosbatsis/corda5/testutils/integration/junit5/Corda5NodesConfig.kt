package com.github.manosbatsis.corda5.testutils.integration.junit5

data class Corda5NodesConfig(
    val authUsername: String = "admin",
    val authPassword: String = "admin",
    val baseUrl: String = "https://localhost:8888/api/v1/",
    val httpMaxWaitSeconds: Int = 60,
    val debug: Boolean = false
)
