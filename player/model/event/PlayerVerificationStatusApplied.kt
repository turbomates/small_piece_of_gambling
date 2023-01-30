package io.betforge.player.model.event

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.model.Status
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerVerificationStatusApplied(
    @Serializable(with = UUIDSerializer::class) val playerId: UUID,
    val status: Status
) : Event() {
    override val key
        get() = Companion

    companion object : Event.Key<PlayerVerificationStatusApplied>
}
