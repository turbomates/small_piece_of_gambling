package io.betforge.player.application.limits.admin.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.limits.admin.AdminApplicationLimitsTable
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.UUID

class ApplicationLimitsQO(
    private val applicationId: UUID
) : QueryObject<List<FinancialLimit>> {
    override suspend fun getData(): List<FinancialLimit> {
        return AdminFinancialLimitsTable
            .join(AdminMoneyLimitsTable, JoinType.INNER, AdminMoneyLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .join(AdminApplicationLimitsTable, JoinType.LEFT, AdminApplicationLimitsTable.limitId, AdminFinancialLimitsTable.id)
            .select { AdminApplicationLimitsTable.application eq applicationId }
            .map { it.toFinancialLimit() }
    }
}
