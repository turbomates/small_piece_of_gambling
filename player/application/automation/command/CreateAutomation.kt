@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.automation.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.player.model.automation.Automation
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class CreateAutomation(
    val groupId: UUID,
    val trigger: Automation.Trigger,
    val issueId: UUID,
    val issueType: Automation.IssueType,
    val shouldExecuteOnCreate: Boolean,
    val period: Period? = null
)
