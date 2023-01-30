package io.betforge.player.application.restriction.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.restrictions.EntryRestrictionsTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.UUID

class CoolingOffPeriodQO(private val playerId: UUID) : QueryObject<EntryRestrictions> {
    override suspend fun getData(): EntryRestrictions {
        return EntryRestrictionsTable.select {
            EntryRestrictionsTable.playerId eq playerId and
                (EntryRestrictionsTable.type eq io.betforge.player.model.restrictions.EntryRestrictions.Type.COOLING_OFF_PERIOD) and
                EntryRestrictionsTable.endedAt.greater(LocalDateTime.now())
        }.single().toEntryRestrictions()
    }
}
