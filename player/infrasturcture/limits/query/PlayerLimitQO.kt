package io.betforge.player.infrasturcture.limits.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.infrastructure.domain.Currency
import io.betforge.player.model.limits.player.FinancialLimits
import io.betforge.player.model.limits.player.FinancialLimitsTable
import io.betforge.player.model.limits.player.LimitsMoneyTable
import io.betforge.player.model.limits.player.UsedLimitsMoneyTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.UUID

class PlayerLimitQO(
    private val playerId: UUID,
    private val type: FinancialLimits.Type,
    private val currency: Currency
) : QueryObject<List<PlayerLimit>> {
    override suspend fun getData(): List<PlayerLimit> {
        return FinancialLimitsTable
            .join(LimitsMoneyTable, JoinType.INNER, LimitsMoneyTable.limitId, FinancialLimitsTable.id)
            .join(UsedLimitsMoneyTable, JoinType.INNER, UsedLimitsMoneyTable.limitId, LimitsMoneyTable.limitId)
            .select {
                FinancialLimitsTable.type eq type and
                    (FinancialLimitsTable.playerId eq playerId) and
                    (LimitsMoneyTable.money.currency eq currency)
            }
            .map { it.toPlayerLimit() }
    }
}

fun ResultRow.toPlayerLimit() = PlayerLimit(
    this[FinancialLimitsTable.id].value,
    this[FinancialLimitsTable.period],
    this[LimitsMoneyTable.money],
    this[LimitsMoneyTable.startedAt],
    this[LimitsMoneyTable.endedAt],
    this[UsedLimitsMoneyTable.id].value,
    this[UsedLimitsMoneyTable.used],
    this[UsedLimitsMoneyTable.calculatedFrom]
)
