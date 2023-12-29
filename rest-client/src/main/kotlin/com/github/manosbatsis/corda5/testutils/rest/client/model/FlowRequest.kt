package com.github.manosbatsis.corda5.testutils.rest.client.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

data class FlowRequest<T>(
    val clientRequestId: String = UUID.randomUUID().toString(),
    val flowClassName: String,
    val requestBody: Any?,
    @JsonIgnore
    val flowResultClass: Class<T>,
) {
    constructor(
        clientRequestId: String = UUID.randomUUID().toString(),
        flowClass: Class<*>,
        requestBody: Any?,
        flowResultClass: Class<T>,
    ) : this(clientRequestId, flowClass.canonicalName, requestBody, flowResultClass)

    fun <N> withFlowResultClass(other: Class<N>) = FlowRequest(
        clientRequestId = clientRequestId,
        flowClassName = flowClassName,
        requestBody = requestBody,
        flowResultClass = other
    )
}
