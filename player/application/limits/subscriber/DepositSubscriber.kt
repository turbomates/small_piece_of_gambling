package io.betforge.player.application.limits.subscriber

import com.google.inject.Inject
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.payment.model.payment.event.MoneyDeposited
import io.betforge.player.model.limits.player.DepositLimitRepository

class DepositSubscriber @Inject constructor(
    private val repository: DepositLimitRepository,
    private val transaction: TransactionManager
) : EventSubscriber<MoneyDeposited> {

    override suspend fun invoke(event: MoneyDeposited) {
        transaction {
            repository.getPlayerLimits(event.userId).forEach {
                it.use(event.deposit)
            }
        }
    }
}
