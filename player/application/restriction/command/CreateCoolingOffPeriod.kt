package io.betforge.player.application.restriction.command

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class CreateCoolingOffPeriod(
    @Serializable(with = LocalDateTimeSerializer::class)
    val endedAt: LocalDateTime
) {
    @Transient
    lateinit var playerId: UUID
}
