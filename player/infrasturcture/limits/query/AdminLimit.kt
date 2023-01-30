package io.betforge.player.infrasturcture.limits.query

import io.betforge.infrastructure.domain.Money
import io.betforge.player.infrasturcture.limits.RelationType
import io.betforge.player.model.limits.admin.FinancialLimits
import java.util.UUID

data class AdminLimit(
    val id: UUID,
    val period: FinancialLimits.Period,
    val type: FinancialLimits.Type,
    val limitMoney: Money,
    val groupPlayers: Array<UUID>?,
    val playerId: UUID?,
    val applicationId: UUID?,
    val priority: RelationType
) {
    fun isExceeding(defaultUsedMoney: Money): Boolean {
        return limitMoney < defaultUsedMoney
    }
}
