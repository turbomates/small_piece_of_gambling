package io.betforge.player.application.group

import com.google.inject.Inject
import dev.tmsoft.lib.event.Event
import dev.tmsoft.lib.event.EventSubscriber
import dev.tmsoft.lib.event.EventsSubscriber
import dev.tmsoft.lib.exposed.query.QueryExecutor
import io.betforge.player.application.filter.event.PlayerFilterApplied
import io.betforge.player.application.group.command.Handler
import io.betforge.player.application.group.command.UpdatePlayersInGroup
import io.betforge.player.application.group.queryobject.GroupsByFilterQO

class GroupSubscriber @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val handler: Handler
) : EventsSubscriber {
    override fun subscribers(): List<EventsSubscriber.EventSubscriberItem<out Event>> {
        return listOf(
            PlayerFilterApplied to object : EventSubscriber<PlayerFilterApplied> {
                override suspend fun invoke(event: PlayerFilterApplied) {
                    val groups = queryExecutor.execute(GroupsByFilterQO(event.filterId))
                    groups.forEach {
                        handler.handle(UpdatePlayersInGroup(it.id, event.playerIds))
                    }
                }
            }
        )
    }
}
