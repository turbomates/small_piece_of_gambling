package io.betforge.player.application.limits.subscriber

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.casino.model.softswiss.event.GamePlayed
import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.limits.player.WagerLimitRepository
import java.util.UUID

class BetPlacedSubscriber @Inject constructor(
    private val repository: WagerLimitRepository,
    private val transaction: TransactionManager
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            GamePlayed to object : EventSubscriber<GamePlayed> {
                override suspend fun invoke(event: GamePlayed) {
                    if (event.bet.isPositive) {
                        increaseCurrentBetLimit(event.userId, event.bet)
                    }
                }
            }
        )
    }

    private suspend fun increaseCurrentBetLimit(playerId: UUID, placed: Money) {
        transaction {
            repository.getPlayerLimits(playerId).forEach { it.use(placed) }
        }
    }
}
