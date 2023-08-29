package com.github.manosbatsis.corda5.testutils.integration.junit5.client.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

/**
 * The status of a flow. Mirrors [net.corda.flow.rest.v1.types.response.FlowStatusResponse]
 *
 * @param holdingIdentityShortHash The short form hash of the Holding Identity
 * @param clientRequestId The unique ID supplied by the client when the flow was created.
 * @param flowId The internal unique ID for the flow.
 * @param flowStatus The current state of the executing flow.
 * @param flowResult The result returned from a completed flow, only set when the flow status is 'COMPLETED' otherwise
 * null
 * @param flowError The details of the error that caused a flow to fail, only set when the flow status is 'FAILED'
 * otherwise null
 * @param timestamp The timestamp of when the status was last updated (in UTC)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FlowStatusResponse(
    val holdingIdentityShortHash: String,
    val clientRequestId: String?,
    val flowId: String?,
    val flowStatus: String,
    val flowResult: String?,
    val flowError: FlowStateErrorResponse?,
    val timestamp: Instant
) {
    companion object {
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
        val finalStatuses = setOf(COMPLETED, FAILED)
    }

    @JsonIgnore
    fun isFinal() = finalStatuses.contains(this.flowStatus)

    @JsonIgnore
    fun isError() = FAILED == this.flowStatus

    @JsonIgnore
    fun isSuccess() = COMPLETED == this.flowStatus
}
