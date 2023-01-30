package io.betforge.player.infrasturcture.serializer

import io.betforge.infrastructure.domain.Money
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = Money::class)
object FilterMoneySerializer : KSerializer<Money> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FilterMoneySerializerDescriptor")

    override fun deserialize(decoder: Decoder): Money {
        return Money.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Money) {
        encoder.encodeString("${value.coins()} ${value.currency}")
    }
}
