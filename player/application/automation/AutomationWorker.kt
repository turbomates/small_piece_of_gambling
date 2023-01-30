package io.betforge.player.application.automation

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.worker.Worker
import io.betforge.infrastructure.extensions.time.plusPeriod
import io.betforge.player.application.automation.command.ExecuteAutomation
import io.betforge.player.application.automation.command.Handler
import io.betforge.player.application.automation.queryobject.AutomationsByTriggerQO
import io.betforge.player.application.group.queryobject.GroupPlayerIdsQO
import io.betforge.player.infrasturcture.exceptions.AutomationException
import io.betforge.player.model.automation.Automation
import java.time.LocalDateTime

const val INTERVAL = 60L * 1000L

class AutomationWorker @Inject constructor(private val handler: Handler, private val queryExecutor: QueryExecutor) : Worker(INTERVAL) {
    override suspend fun process() {
        val automations = queryExecutor.execute(AutomationsByTriggerQO(Automation.Trigger.PERIODIC))
        automations.forEach { automation ->
            if (automation.period == null) throw AutomationException("Cannot execute periodic automation without period")
            val lastExecutionDateTime = automation.executedAt ?: automation.createdAt
            if (lastExecutionDateTime.plusPeriod(automation.period) > LocalDateTime.now()) {
                val playerIds = queryExecutor.execute(GroupPlayerIdsQO(automation.groupId))
                playerIds.forEach { handler.handle(ExecuteAutomation(automation.id, it)) }
            }
        }
    }
}
