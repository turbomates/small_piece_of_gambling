package io.betforge.player.infrasturcture.limits

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.exchange.PointExchanger
import io.betforge.player.application.statistic.query.AdminLimitDefaultSumQO
import io.betforge.player.infrasturcture.limits.query.AdminLimit
import io.betforge.player.infrasturcture.limits.query.AdminLimitQO
import io.betforge.player.infrasturcture.limits.query.PlayerLimit
import io.betforge.player.infrasturcture.limits.query.PlayerLimitQO
import io.betforge.player.model.limits.player.FinancialLimitsRepository
import java.util.UUID
import io.betforge.player.model.limits.admin.FinancialLimits.Type as AdminLimitType
import io.betforge.player.model.limits.player.FinancialLimits.Type as PlayerLimitType

typealias AdminLimits = suspend (playerId: UUID, type: AdminLimitType) -> List<AdminLimit>
typealias PlayerLimits = suspend (playerId: UUID, type: PlayerLimitType, currency: Currency) -> List<PlayerLimit>

class LimitValidator @Inject constructor(
    private val transaction: TransactionManager,
    private val exchanger: PointExchanger,
    private val limitRepository: FinancialLimitsRepository
) {
    private val adminLimits: AdminLimits = { playerId, type -> AdminLimitQO(playerId, type).getData() }
    private val playerLimits: PlayerLimits = { playerId, type, currency -> PlayerLimitQO(playerId, type, currency).getData() }

    suspend fun isLimitExceeded(
        amount: Money,
        playerId: UUID,
        limitType: LimitType
    ): Boolean {
        return when (limitType) {
            LimitType.WAGER_LIMIT -> isAdminLimitsExceeded(playerId, AdminLimitType.WAGER_LIMIT, amount) ||
                isPlayerLimitsExceeded(playerId, PlayerLimitType.WAGER_LIMIT, amount)
            LimitType.LOSS_LIMIT -> isAdminLimitsExceeded(playerId, AdminLimitType.LOSS_LIMIT, amount) ||
                isPlayerLimitsExceeded(playerId, PlayerLimitType.LOSS_LIMIT, amount)
            LimitType.DEPOSIT_LIMIT -> isAdminLimitsExceeded(playerId, AdminLimitType.DEPOSIT_LIMIT, amount) ||
                isPlayerLimitsExceeded(playerId, PlayerLimitType.DEPOSIT_LIMIT, amount)
            LimitType.MAX_BET_LIMIT -> adminLimits(playerId, AdminLimitType.MAX_BET_LIMIT).any {
                it.isExceeding(exchanger.exchangeToDefault(amount))
            }
            LimitType.WITHDRAW_LIMIT -> isAdminLimitsExceeded(playerId, AdminLimitType.WITHDRAW_LIMIT, amount)
        }
    }

    private suspend fun isPlayerLimitsExceeded(
        playerId: UUID,
        type: PlayerLimitType,
        actionMoney: Money
    ): Boolean {
        return transaction {
            playerLimits(playerId, type, actionMoney.currency).any { limit ->
                limitRepository[limit.id].isExceeding(actionMoney, limit.limitMoney)
            }
        }
    }

    private suspend fun isAdminLimitsExceeded(
        playerId: UUID,
        type: AdminLimitType,
        actionMoney: Money
    ): Boolean {
        return transaction {
            val exchangedDeposit = exchanger.exchangeToDefault(actionMoney)
            adminLimits(playerId, type).any { limit ->
                val usedDeposit = AdminLimitDefaultSumQO(playerId, limit.period, type).getData()
                limit.isExceeding(usedDeposit?.let { exchangedDeposit + usedDeposit } ?: exchangedDeposit)
            }
        }
    }
}

enum class LimitType {
    WAGER_LIMIT, LOSS_LIMIT, DEPOSIT_LIMIT, WITHDRAW_LIMIT, MAX_BET_LIMIT
}
