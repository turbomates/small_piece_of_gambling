package io.betforge.player.model.automation

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import dev.tmsoft.lib.exposed.type.jsonb
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.player.model.event.automation.AutomationCreated
import io.betforge.player.model.event.automation.BonusAutomationExecuted
import io.betforge.player.model.event.automation.EmailAutomationExecuted
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

class Automation private constructor(id: EntityID<UUID>) : Entity<UUID>(id), AggregateRoot {
    private var groupId by PlayerAutomationTable.groupId
    private var trigger by PlayerAutomationTable.trigger
    private var issueId by PlayerAutomationTable.issueId
    private var issueType by PlayerAutomationTable.issueType
    private var shouldExecuteOnCreate by PlayerAutomationTable.shouldExecuteOnCreate
    private var period by PlayerAutomationTable.period
    private var executedAt by PlayerAutomationTable.executedAt

    companion object : PrivateEntityClass<UUID, Automation>(object : Repository() {}) {
        fun create(groupId: UUID, trigger: Trigger, issue: Issue, executeOnCreate: Boolean, period: Period?): Automation {
            return Automation.new {
                this.groupId = groupId
                this.trigger = trigger
                this.issueId = issue.id
                this.issueType = issue.type
                this.shouldExecuteOnCreate = executeOnCreate
                this.period = period
            }.also { it.addEvent(AutomationCreated(it.id.value, it.shouldExecuteOnCreate, it.groupId)) }
        }
    }

    fun execute(playerIds: List<UUID>) {
        executedAt = LocalDateTime.now()
        val event = when (issueType) {
            IssueType.BONUS -> BonusAutomationExecuted(id.value, playerIds, issueId)
            IssueType.SEND_EMAIL -> EmailAutomationExecuted(id.value)
        }

        addEvent(event)
    }

    abstract class Repository : EntityClass<UUID, Automation>(PlayerAutomationTable, Automation::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): Automation {
            return Automation(entityId)
        }
    }

    @Serializable
    enum class Trigger {
        JOIN_GROUP,
        LEAVE_GROUP,
        DEPOSIT,
        PERIODIC
    }

    @Serializable
    enum class IssueType {
        BONUS,
        SEND_EMAIL
    }

    data class Issue(val id: UUID, val type: IssueType)
}

object PlayerAutomationTable : UUIDTable("player_automations") {
    val groupId = uuid("group_id")
    val trigger = enumerationByName("trigger", 25, Automation.Trigger::class)
    val issueId = uuid("issue_id")
    val issueType = enumerationByName("issue_type", 25, Automation.IssueType::class)
    val shouldExecuteOnCreate = bool("execute_on_create")
    val period = jsonb("period", Period.serializer()).nullable()
    val executedAt = datetime("executed_at").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
