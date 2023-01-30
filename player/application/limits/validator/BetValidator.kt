package io.betforge.player.application.limits.validator

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.validation.Error
import io.betforge.casino.application.softswiss.game.PlayerLimitValidator
import io.betforge.infrastructure.domain.Money
import io.betforge.player.infrasturcture.limits.LimitType
import io.betforge.player.infrasturcture.limits.LimitValidator
import org.valiktor.Constraint
import java.util.UUID

class BetValidator @Inject constructor(
    private val limitValidator: LimitValidator,
    private val transaction: TransactionManager
) : PlayerLimitValidator {
    override suspend fun validate(bet: Money, playerId: UUID): List<Error> {
        return transaction {
            mutableListOf<Error>().apply {
                if (limitValidator.isLimitExceeded(bet, playerId, LimitType.WAGER_LIMIT)) {
                    add(Error(WageringLimitExceed.name))
                }
                if (limitValidator.isLimitExceeded(bet, playerId, LimitType.LOSS_LIMIT)) {
                    add(Error(LossLimitExceed.name))
                }
                if (limitValidator.isLimitExceeded(bet, playerId, LimitType.MAX_BET_LIMIT)) {
                    add(Error(MaxBetLimitExceed.name))
                }
            }
        }
    }

    private object WageringLimitExceed : Constraint {
        override val name: String
            get() = "Betting limit exceeded"
    }

    private object LossLimitExceed : Constraint {
        override val name: String
            get() = "Loss limit exceeded"
    }

    private object MaxBetLimitExceed : Constraint {
        override val name: String
            get() = "Max bet limit exceeded"
    }
}
