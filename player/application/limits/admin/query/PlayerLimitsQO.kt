package io.betforge.player.application.limits.admin.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import io.betforge.player.model.limits.admin.AdminPlayersLimitsTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.UUID

class PlayerLimitsQO(
    private val playerId: UUID
) : QueryObject<List<FinancialLimit>> {
    override suspend fun getData(): List<FinancialLimit> {
        return AdminFinancialLimitsTable
            .join(AdminMoneyLimitsTable, JoinType.INNER, AdminMoneyLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminPlayersLimitsTable, JoinType.LEFT, AdminPlayersLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .select { AdminPlayersLimitsTable.playerId eq playerId }
            .map { it.toFinancialLimit() }
    }
}
