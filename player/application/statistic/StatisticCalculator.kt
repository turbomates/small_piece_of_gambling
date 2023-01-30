package io.betforge.player.application.statistic

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.exposed.withDataBaseLock
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.exchange.PointExchanger
import io.betforge.player.application.statistic.query.PlayerStatistic
import io.betforge.player.application.statistic.query.PlayerStatisticQO
import io.betforge.player.model.statistic.PlayerStatisticTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

class StatisticCalculator @Inject constructor(
    private val exchanger: PointExchanger,
    private val queryExecutor: QueryExecutor,
    private val transaction: TransactionManager
) {
    suspend fun apply(data: PlayerAction) {
        transaction {
            val defaultData = data.toDefaultAction(exchanger)
            withDataBaseLock(data.money.hashCode()) {
                val statistics = queryExecutor.execute(
                    PlayerStatisticQO(
                        playerId = data.playerId,
                        date = LocalDate.now(),
                        currencies = listOf(data.money.currency, Currency.POINT)
                    )
                ).associateBy { if (it.win.currency == Currency.POINT) defaultData else data }

                if (statistics.isNotEmpty()) {
                    statistics.forEach { (data, statistic) -> update(data, statistic) }
                } else {
                    insert(data); insert(defaultData)
                }
            }
        }
    }

    private fun update(data: PlayerAction, statistic: PlayerStatistic) {
        PlayerStatisticTable.update({
            PlayerStatisticTable.playerId eq statistic.playerId and
                (PlayerStatisticTable.date eq statistic.date) and
                (PlayerStatisticTable.win.currency eq statistic.win.currency)
        }) {
            when (data) {
                is WinAction -> {
                    it[win] = statistic.win + data.money
                    it[wager] = statistic.wager + data.bet
                }
                is LossAction -> {
                    it[loss] = statistic.loss + data.money
                    it[wager] = statistic.wager + data.bet
                }
                is DepositAction -> it[deposit] = statistic.deposit + data.money
                is WithdrawAction -> it[withdraw] = statistic.withdraw + data.money
            }
        }
    }

    private fun insert(data: PlayerAction) {
        val zeroMoney = data.money.zero()
        PlayerStatisticTable.insert {
            it[date] = LocalDate.now()
            it[playerId] = data.playerId
            it[win] = zeroMoney
            it[loss] = zeroMoney
            it[wager] = zeroMoney
            it[deposit] = zeroMoney
            it[withdraw] = zeroMoney
            when (data) {
                is WinAction -> {
                    it[win] = data.money; it[wager] = data.bet
                }
                is LossAction -> {
                    it[loss] = data.money; it[wager] = data.bet
                }
                is DepositAction -> it[deposit] = data.money
                is WithdrawAction -> it[withdraw] = data.money
            }
        }
    }
}
