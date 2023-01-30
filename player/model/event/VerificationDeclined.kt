package io.betforge.player.model.event

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class VerificationDeclined(
    @Serializable(with = UUIDSerializer::class) val playerId: UUID,
    val type: String
) : Event() {
    override val key
        get() = Companion

    companion object : Event.Key<VerificationDeclined>
}
