package io.betforge.player.application.filter.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.PlayerFilterTable
import org.jetbrains.exposed.sql.select
import java.util.UUID

class FilterExistQO(private val filterId: UUID) : QueryObject<Boolean> {
    override suspend fun getData(): Boolean {
        return !PlayerFilterTable
            .select { PlayerFilterTable.id eq filterId }
            .empty()
    }
}
