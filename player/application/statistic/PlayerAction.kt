package io.betforge.player.application.statistic

import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.exchange.PointExchanger
import java.util.UUID

sealed class PlayerAction(val playerId: UUID, val money: Money)

class DepositAction(playerId: UUID, deposit: Money) : PlayerAction(playerId, deposit)
class WinAction(playerId: UUID, win: Money, val bet: Money) : PlayerAction(playerId, win)
class LossAction(playerId: UUID, loss: Money, val bet: Money) : PlayerAction(playerId, loss)
class WithdrawAction(playerId: UUID, withdraw: Money) : PlayerAction(playerId, withdraw)

suspend fun PlayerAction.toDefaultAction(exchanger: PointExchanger): PlayerAction {
    return when (this) {
        is DepositAction -> DepositAction(playerId, exchanger.exchangeToDefault(money))
        is WinAction -> WinAction(playerId, exchanger.exchangeToDefault(money), exchanger.exchangeToDefault(bet))
        is LossAction -> WinAction(playerId, exchanger.exchangeToDefault(money), exchanger.exchangeToDefault(bet))
        is WithdrawAction -> WithdrawAction(playerId, exchanger.exchangeToDefault(money))
    }
}
