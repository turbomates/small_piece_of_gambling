package io.betforge.player.application.automation

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import dev.tmsoft.lib.exposed.query.QueryExecutor
import io.betforge.payment.model.payment.event.MoneyDeposited
import io.betforge.player.application.automation.command.ExecuteAutomation
import io.betforge.player.application.automation.command.Handler
import io.betforge.player.application.automation.queryobject.AutomationIdsByGroupTriggerQO
import io.betforge.player.application.group.queryobject.GroupIdsByUserId
import io.betforge.player.application.group.queryobject.GroupPlayerIdsQO
import io.betforge.player.model.automation.Automation
import io.betforge.player.model.event.automation.AutomationCreated
import io.betforge.player.model.event.group.PlayerAddedToGroup
import io.betforge.player.model.event.group.PlayerRemovedFromGroup

class AutomationSubscriber @Inject constructor(
    private val handler: Handler,
    private val queryExecutor: QueryExecutor
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            AutomationCreated to object : EventSubscriber<AutomationCreated> {
                override suspend fun invoke(event: AutomationCreated) {
                    if (event.shouldExecuteOnCreate) {
                        val playerIds = queryExecutor.execute(GroupPlayerIdsQO(event.groupId))
                        playerIds.forEach { handler.handle(ExecuteAutomation(event.automationId, it)) }
                    }
                }
            },
            PlayerAddedToGroup to object : EventSubscriber<PlayerAddedToGroup> {
                override suspend fun invoke(event: PlayerAddedToGroup) {
                    val automationIds =
                        queryExecutor.execute(AutomationIdsByGroupTriggerQO(listOf(event.groupId), Automation.Trigger.JOIN_GROUP))
                    automationIds.forEach { handler.handle(ExecuteAutomation(it, event.playerId)) }
                }
            },
            PlayerRemovedFromGroup to object : EventSubscriber<PlayerRemovedFromGroup> {
                override suspend fun invoke(event: PlayerRemovedFromGroup) {
                    val automationIds =
                        queryExecutor.execute(AutomationIdsByGroupTriggerQO(listOf(event.groupId), Automation.Trigger.LEAVE_GROUP))
                    automationIds.forEach { handler.handle(ExecuteAutomation(it, event.playerId)) }
                }
            },
            MoneyDeposited to object : EventSubscriber<MoneyDeposited> {
                override suspend fun invoke(event: MoneyDeposited) {
                    val groupIds = queryExecutor.execute(GroupIdsByUserId(event.userId))
                    val automationIds = queryExecutor.execute(AutomationIdsByGroupTriggerQO(groupIds, Automation.Trigger.DEPOSIT))
                    automationIds.forEach { handler.handle(ExecuteAutomation(it, event.userId)) }
                }
            }
        )
    }
}
