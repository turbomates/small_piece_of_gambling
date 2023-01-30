package io.betforge.player.application.limits.subscriber

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.casino.model.softswiss.event.GamePlayed
import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.limits.player.LossLimitRepository
import java.util.UUID

class BetLostSubscriber @Inject constructor(
    private val repository: LossLimitRepository,
    private val transaction: TransactionManager
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            GamePlayed to object : EventSubscriber<GamePlayed> {
                override suspend fun invoke(event: GamePlayed) {
                    if (event.win.isNegative) {
                        increaseCurrentLimit(event.userId, event.win.abs())
                    }
                }
            }
        )
    }

    private suspend fun increaseCurrentLimit(playerId: UUID, lost: Money) {
        transaction {
            repository.getPlayerLimits(playerId).forEach {
                it.use(lost)
            }
        }
    }
}
