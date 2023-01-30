package io.betforge.player.application.automation.command

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.player.infrasturcture.automation.AutomationRepository
import io.betforge.player.model.automation.Automation

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    private val repository: AutomationRepository
) {
    suspend fun handle(command: CreateAutomation) {
        transaction {
            Automation.create(
                command.groupId,
                command.trigger,
                Automation.Issue(command.issueId, command.issueType),
                command.shouldExecuteOnCreate,
                command.period
            )
        }
    }

    suspend fun handle(command: ExecuteAutomation) {
        transaction {
            val automation = repository.getById(command.automationId)
            automation.execute(listOf(command.playerId))
        }
    }
}
