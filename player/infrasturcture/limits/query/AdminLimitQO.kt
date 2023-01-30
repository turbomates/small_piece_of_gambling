package io.betforge.player.infrasturcture.limits.query

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.exposed.type.any
import io.betforge.player.infrasturcture.limits.toRelation
import io.betforge.player.model.group.PlayerGroupsTable
import io.betforge.player.model.limits.admin.AdminApplicationLimitsTable
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminGroupsLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import io.betforge.player.model.limits.admin.AdminPlayersLimitsTable
import io.betforge.player.model.limits.admin.FinancialLimits
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.util.UUID

class AdminLimitQO(
    private val playerId: UUID,
    private val type: FinancialLimits.Type
) : QueryObject<List<AdminLimit>> {
    override suspend fun getData(): List<AdminLimit> {
        return AdminFinancialLimitsTable
            .join(AdminMoneyLimitsTable, JoinType.INNER, AdminMoneyLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminGroupsLimitsTable, JoinType.LEFT, AdminGroupsLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminPlayersLimitsTable, JoinType.LEFT, AdminPlayersLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminApplicationLimitsTable, JoinType.LEFT, AdminApplicationLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(PlayerGroupsTable, JoinType.LEFT, PlayerGroupsTable.id, AdminGroupsLimitsTable.playerGroupId)
            .select {
                AdminApplicationLimitsTable.application.isNotNull() or
                    (AdminPlayersLimitsTable.playerId eq playerId) or
                    (PlayerGroupsTable.playerIds any playerId) and
                    (AdminFinancialLimitsTable.type eq type)
            }
            .map { it.toAdminLimit() }
            .let { limits ->
                if (limits.isNotEmpty()) {
                    val maxPriority = limits.maxOf { it.priority.order }
                    limits.filter { it.priority.order == maxPriority }
                } else limits
            }
    }
}

private fun ResultRow.toAdminLimit() = AdminLimit(
    this[AdminFinancialLimitsTable.id].value,
    this[AdminFinancialLimitsTable.period],
    this[AdminFinancialLimitsTable.type],
    this[AdminMoneyLimitsTable.money],
    this.getOrNull(PlayerGroupsTable.playerIds),
    this.getOrNull(AdminPlayersLimitsTable.playerId),
    this.getOrNull(AdminApplicationLimitsTable.application),
    toRelation()
)
