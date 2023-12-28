package com.github.manosbatsis.corda5.testutils.integration.junit5.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.corda.v5.base.types.MemberX500Name

open class MemberX500NameSerializer : StdSerializer<MemberX500Name>(MemberX500Name::class.java) {

    override fun serialize(memberX500Name: MemberX500Name, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(memberX500Name.toString())
    }
}

open class MemberX500NameDeserializer : StdDeserializer<MemberX500Name>(MemberX500Name::class.java) {
    override fun deserialize(jsonParser: JsonParser, obj: DeserializationContext): MemberX500Name {
        val value: String = jsonParser.codec.readValue(jsonParser, String::class.java)

        return MemberX500Name.parse(value)
    }
}