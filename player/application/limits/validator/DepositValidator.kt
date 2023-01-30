package io.betforge.player.application.limits.validator

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.domain.Money
import io.betforge.payment.application.PlayerLimitValidator
import io.betforge.player.infrasturcture.limits.LimitType
import io.betforge.player.infrasturcture.limits.LimitValidator
import org.valiktor.Constraint
import java.util.UUID

class DepositValidator @Inject constructor(
    private val limitValidator: LimitValidator,
    private val transaction: TransactionManager
) : PlayerLimitValidator {
    override suspend fun validate(amount: Money, playerId: UUID): List<Error> {
        return transaction {
            mutableListOf<Error>().apply {
                if (limitValidator.isLimitExceeded(amount, playerId, LimitType.DEPOSIT_LIMIT)) {
                    add(Error(DepositLimitExceed.name))
                }
            }
        }
    }

    private object DepositLimitExceed : Constraint {
        override val name: String
            get() = "Deposit limit exceeded"
    }
}
