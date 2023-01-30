package io.betforge.player.application.statistic.query

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.exposed.timescale.sql.function.timeBucket
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.extensions.time.PeriodType
import io.betforge.infrastructure.extensions.time.startOf
import io.betforge.player.infrasturcture.exposed.TruncPeriod
import io.betforge.player.infrasturcture.exposed.dateTrunc
import io.betforge.player.model.limits.admin.FinancialLimits
import io.betforge.player.model.statistic.PlayerStatisticTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import java.time.LocalDate
import java.util.UUID

class AdminLimitDefaultSumQO(
    private val playerId: UUID,
    private val limitPeriod: FinancialLimits.Period,
    private val limitType: FinancialLimits.Type
) : QueryObject<Money?> {
    override suspend fun getData(): Money? {
        val limit = when (limitType) {
            FinancialLimits.Type.LOSS_LIMIT -> PlayerStatisticTable.loss
            FinancialLimits.Type.WAGER_LIMIT -> PlayerStatisticTable.wager
            FinancialLimits.Type.DEPOSIT_LIMIT -> PlayerStatisticTable.deposit
            FinancialLimits.Type.WITHDRAW_LIMIT -> PlayerStatisticTable.withdraw
            FinancialLimits.Type.MAX_BET_LIMIT -> throw IllegalArgumentException("Have no statistic values for this type: $limitType")
        }
        val (timeBucket, intervalOp) = when (limitPeriod) {
            FinancialLimits.Period.DAILY -> PlayerStatisticTable.date.timeBucket("1 day") to
                Op.build { PlayerStatisticTable.date eq LocalDate.now() }
            FinancialLimits.Period.WEEKLY -> PlayerStatisticTable.date.timeBucket("1 week") to
                Op.build { PlayerStatisticTable.date greaterEq LocalDate.now().startOf(PeriodType.WEEKS) }
            FinancialLimits.Period.MONTHLY -> PlayerStatisticTable.date.dateTrunc(TruncPeriod.MONTH) to
                Op.build { PlayerStatisticTable.date greaterEq LocalDate.now().startOf(PeriodType.MONTHS) }
            FinancialLimits.Period.NONE -> throw IllegalArgumentException("Have no statistic values for this period: $limitPeriod")
        }

        return PlayerStatisticTable
            .slice(limit.amount.sum(), timeBucket)
            .select {
                PlayerStatisticTable.playerId eq playerId and
                    (limit.currency eq Currency.POINT) and intervalOp
            }
            .groupBy(timeBucket)
            .firstOrNull()
            ?.let { Money(it[limit.amount.sum()] ?: 0, Currency.POINT) }
    }
}
