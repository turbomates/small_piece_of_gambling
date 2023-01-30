package io.betforge.player.model.statistic

import io.betforge.infrastructure.domain.money
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object PlayerStatisticTable : Table("statistic.players_daily") {
    val playerId = uuid("player_id")
    val date = date("date")
    val win = money("win_")
    val loss = money("loss_")
    val wager = money("wager_")
    val deposit = money("deposit_")
    val withdraw = money("withdraw_")
}
