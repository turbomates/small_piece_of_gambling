package io.betforge.player.infrasturcture.automation

import io.betforge.infrastructure.NotFound
import io.betforge.player.model.automation.Automation
import io.betforge.player.model.automation.PlayerAutomationTable
import org.jetbrains.exposed.sql.select
import java.util.UUID

class AutomationRepository : Automation.Repository() {
    fun getById(id: UUID): Automation {
        return PlayerAutomationTable
            .select { PlayerAutomationTable.id eq id }
            .singleOrNull()
            ?.let { wrapRow(it) } ?: throw NotFound("Automation not found")
    }
}
