package io.betforge.player.infrasturcture.serializer

import io.betforge.player.infrasturcture.filter.Range
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

@Serializer(forClass = Range::class)
open class FilterRangeSerializer<T>(val serializer: KSerializer<T>) : KSerializer<Range<T>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FilterRangeSerializerDescriptor")

    override fun deserialize(decoder: Decoder): Range<T> {
        val (fromString, toString) = decoder.decodeString().split("~")
        val from = if (fromString.isEmpty()) null else Json.decodeFromJsonElement(serializer, JsonPrimitive(fromString))
        val to = if (toString.isEmpty()) null else Json.decodeFromJsonElement(serializer, JsonPrimitive(toString))
        return Range(from, to)
    }

    override fun serialize(encoder: Encoder, value: Range<T>) {
        val from = value.from?.let { Json.encodeToString(serializer, it).unquote() }.orEmpty()
        val to = value.to?.let { Json.encodeToString(serializer, it).unquote() }.orEmpty()
        encoder.encodeString("$from~$to")
    }

    private fun String.unquote(): String {
        if (first() == '"' && last() == '"') return slice(1..length - 2)
        return this
    }
}
