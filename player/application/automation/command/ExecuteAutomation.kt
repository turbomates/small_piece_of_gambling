@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.automation.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class ExecuteAutomation(val automationId: UUID, val playerId: UUID)
