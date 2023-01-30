package io.betforge.player.model.event

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerRegistered(
    @Serializable(with = UUIDSerializer::class) val id: UUID
) : Event() {
    override val key
        get() = Companion

    companion object : Event.Key<PlayerRegistered>
}
