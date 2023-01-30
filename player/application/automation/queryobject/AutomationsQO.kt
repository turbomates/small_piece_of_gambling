@file:UseSerializers(UUIDSerializer::class, LocalDateTimeSerializer::class)

package io.betforge.player.application.automation.queryobject

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousList
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.player.model.automation.Automation.IssueType
import io.betforge.player.model.automation.Automation.Trigger
import io.betforge.player.model.automation.PlayerAutomationTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.util.UUID

class AutomationsQO(private val paging: PagingParameters) : QueryObject<ContinuousList<Automation>> {
    override suspend fun getData(): ContinuousList<Automation> {
        return PlayerAutomationTable
            .selectAll()
            .orderBy(PlayerAutomationTable.createdAt, SortOrder.DESC)
            .toContinuousList(paging, ResultRow::toAutomation)
    }
}

class AutomationsByTriggerQO(private val trigger: Trigger): QueryObject<List<Automation>> {
    override suspend fun getData(): List<Automation> {
        return PlayerAutomationTable
            .select { PlayerAutomationTable.trigger eq trigger }
            .map { it.toAutomation() }
    }
}

class AutomationIdsByGroupTriggerQO(private val groupIds: List<UUID>, private val trigger: Trigger) : QueryObject<List<UUID>> {
    override suspend fun getData(): List<UUID> {
        return PlayerAutomationTable
            .slice(PlayerAutomationTable.id)
            .select { PlayerAutomationTable.trigger eq trigger and (PlayerAutomationTable.groupId inList groupIds) }
            .map { it[PlayerAutomationTable.id].value }
    }
}

fun ResultRow.toAutomation() = Automation(
    this[PlayerAutomationTable.id].value,
    this[PlayerAutomationTable.groupId],
    this[PlayerAutomationTable.trigger],
    this[PlayerAutomationTable.issueId],
    this[PlayerAutomationTable.issueType],
    this[PlayerAutomationTable.period],
    this[PlayerAutomationTable.executedAt],
    this[PlayerAutomationTable.createdAt],
    this[PlayerAutomationTable.updatedAt]
)

@Serializable
data class Automation(
    val id: UUID,
    val groupId: UUID,
    val trigger: Trigger,
    val issueId: UUID,
    val issueType: IssueType,
    val period: Period? = null,
    val executedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
