package com.github.manosbatsis.corda5.testutils.integration.junit5.nodehandles

import net.corda.v5.base.types.MemberX500Name

data class NodeHandles(
    private val nodes: List<NodeHandle>
) {

    constructor(vararg nodes: NodeHandle) : this(nodes.toList())

    fun getByCommonName(commonName: String) = findByCommonName(commonName)
        ?: error("Could not find node by common name: $commonName")

    fun getByMemberName(memberName: MemberX500Name) = findByMemberName(memberName)
        ?: error("Could not find node by member name: $memberName")

    fun findByCommonName(commonName: String) = nodes
        .filter { it.memberX500Name.commonName == commonName }
        .singleOrNull()

    fun findByMemberName(memberName: MemberX500Name) = nodes
        .filter { it.memberX500Name == memberName }
        .singleOrNull()
}
