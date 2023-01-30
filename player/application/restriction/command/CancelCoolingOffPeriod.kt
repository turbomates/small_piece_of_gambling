package io.betforge.player.application.restriction.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CancelCoolingOffPeriod(
    @Serializable(with = UUIDSerializer::class)
    var playerId: UUID
)
