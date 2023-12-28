package com.github.manosbatsis.corda5.testutils.integration.junit5.jackson

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import net.corda.v5.base.types.MemberX500Name

class Corda5Module : SimpleModule() {
    override fun getModuleName(): String = this.javaClass.simpleName

    override fun setupModule(context: SetupContext) {
        val serializers = SimpleSerializers()
        serializers.addSerializer(MemberX500Name::class.java, MemberX500NameSerializer())
        context.addSerializers(serializers)

        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(MemberX500Name::class.java, MemberX500NameDeserializer())
        context.addDeserializers(deserializers)
    }
}
