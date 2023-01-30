package io.betforge.player.model.event.restriction

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CoolingOffPeriodCreated(
    @Serializable(with = UUIDSerializer::class)
    val playerId: UUID
) : Event() {

    override val key: Key<out Event>
        get() = Companion

    companion object : Key<CoolingOffPeriodCreated>
}
