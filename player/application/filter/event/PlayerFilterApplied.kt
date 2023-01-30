@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.filter.event

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class PlayerFilterApplied(
    val filterId: UUID,
    val playerIds: List<UUID>
) : Event() {
    override val key
        get() = Companion

    companion object : Key<PlayerFilterApplied>
}
