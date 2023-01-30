package io.betforge.player.application.limits.admin.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminGroupsLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.UUID

class GroupLimitsQO(
    private val playerGroupId: UUID
) : QueryObject<List<FinancialLimit>> {
    override suspend fun getData(): List<FinancialLimit> {
        return AdminFinancialLimitsTable
            .join(AdminMoneyLimitsTable, JoinType.INNER, AdminMoneyLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminGroupsLimitsTable, JoinType.LEFT, AdminGroupsLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .select { AdminGroupsLimitsTable.playerGroupId eq playerGroupId }
            .map { it.toFinancialLimit() }
    }
}
