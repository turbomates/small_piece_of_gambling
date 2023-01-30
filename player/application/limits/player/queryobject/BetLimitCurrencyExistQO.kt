package io.betforge.player.application.limits.player.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.infrastructure.domain.Currency
import io.betforge.player.model.limits.player.FinancialLimits
import io.betforge.player.model.limits.player.FinancialLimitsTable
import io.betforge.player.model.limits.player.LimitsMoneyTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.UUID

class BetLimitCurrencyExistQO(private val playerId: UUID, private val currency: Currency) : QueryObject<Boolean> {
    override suspend fun getData(): Boolean {
        return LimitsMoneyTable
            .join(FinancialLimitsTable, JoinType.LEFT, LimitsMoneyTable.limitId, FinancialLimitsTable.id)
            .select {
                LimitsMoneyTable.money.currency eq currency and
                    (LimitsMoneyTable.endedAt eq null) and
                    (FinancialLimitsTable.playerId eq playerId) and
                    (FinancialLimitsTable.type eq FinancialLimits.Type.WAGER_LIMIT)
            }
            .singleOrNull() != null
    }
}
