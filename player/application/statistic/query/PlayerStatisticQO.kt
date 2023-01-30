package io.betforge.player.application.statistic.query

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.statistic.PlayerStatisticTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.LocalDate
import java.util.UUID

class PlayerStatisticQO(
    val playerId: UUID,
    val date: LocalDate,
    val currencies: List<Currency>
) : QueryObject<List<PlayerStatistic>> {
    override suspend fun getData(): List<PlayerStatistic> {
        return PlayerStatisticTable
            .select {
                PlayerStatisticTable.playerId eq playerId and
                    (PlayerStatisticTable.date eq date) and
                    (PlayerStatisticTable.win.currency inList currencies)
            }
            .map { it.toPlayerStatistic() }
    }
}

data class PlayerStatistic(
    val playerId: UUID,
    val date: LocalDate,
    val win: Money,
    val loss: Money,
    val wager: Money,
    val deposit: Money,
    val withdraw: Money
)

fun ResultRow.toPlayerStatistic() = PlayerStatistic(
    this[PlayerStatisticTable.playerId],
    this[PlayerStatisticTable.date],
    this[PlayerStatisticTable.win],
    this[PlayerStatisticTable.loss],
    this[PlayerStatisticTable.wager],
    this[PlayerStatisticTable.deposit],
    this[PlayerStatisticTable.withdraw]
)
