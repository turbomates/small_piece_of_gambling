package io.betforge.player.application.filter

import com.google.inject.Inject
import dev.tmsoft.lib.event.EventStore
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.worker.Worker
import io.betforge.player.application.PlayerIdsQO
import io.betforge.player.application.filter.command.UpdateCountFilter
import io.betforge.player.application.filter.event.PlayerFilterApplied
import io.betforge.player.application.filter.queryobject.AllFiltersQO

const val INTERVAL = 60L * 60L * 1000L

class FilterWorker @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val handler: Handler,
    private val eventStore: EventStore
) : Worker(INTERVAL) {
    override suspend fun process() {
        val filters = queryExecutor.execute(AllFiltersQO())
        filters.forEach {
            val playerIds = queryExecutor.execute(PlayerIdsQO(it.conditions))
            handler.updateCountFilter(UpdateCountFilter(it.id, playerIds.size))
            eventStore.addEvent(PlayerFilterApplied(it.id, playerIds))
        }
    }
}
