package io.betforge.player.application.statistic

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import io.betforge.casino.model.softswiss.event.GamePlayed
import io.betforge.payment.model.payment.event.MoneyDeposited
import io.betforge.payment.model.payment.event.MoneyWithdrawn

class PlayerStatisticSubscriber @Inject constructor(
    private val statistic: StatisticCalculator
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            GamePlayed to object : EventSubscriber<GamePlayed> {
                override suspend fun invoke(event: GamePlayed) {
                    if (event.win.isPositive) {
                        statistic.apply(
                            WinAction(
                                win = event.win,
                                bet = event.bet,
                                playerId = event.userId,
                            )
                        )
                    } else {
                        statistic.apply(
                            LossAction(
                                bet = event.bet,
                                loss = event.bet,
                                playerId = event.userId,
                            )
                        )
                    }
                }
            },
            MoneyDeposited to object : EventSubscriber<MoneyDeposited> {
                override suspend fun invoke(event: MoneyDeposited) {
                    statistic.apply(
                        DepositAction(
                            deposit = event.deposit,
                            playerId = event.userId
                        )
                    )
                }
            },
            MoneyWithdrawn to object : EventSubscriber<MoneyWithdrawn> {
                override suspend fun invoke(event: MoneyWithdrawn) {
                    statistic.apply(
                        WithdrawAction(
                            withdraw = event.withdraw,
                            playerId = event.userId,
                        )
                    )
                }
            }
        )
    }
}
