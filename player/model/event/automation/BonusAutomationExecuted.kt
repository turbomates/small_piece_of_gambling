@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.model.event.automation

import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class BonusAutomationExecuted(
    val automationId: UUID,
    val playerIds: List<UUID>,
    val bonusRuleId: UUID
) : Event() {
    override val key: Key<out Event>
        get() = Companion

    companion object : Key<BonusAutomationExecuted>
}
