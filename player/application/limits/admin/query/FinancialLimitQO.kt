package io.betforge.player.application.limits.admin.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.UUID

class FinancialLimitQO(
    private val limitId: UUID
) : QueryObject<FinancialLimit> {
    override suspend fun getData(): FinancialLimit {
        return AdminFinancialLimitsTable
            .join(AdminMoneyLimitsTable, JoinType.INNER, AdminMoneyLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .select { AdminFinancialLimitsTable.id eq limitId }
            .single()
            .toFinancialLimit()
    }
}
