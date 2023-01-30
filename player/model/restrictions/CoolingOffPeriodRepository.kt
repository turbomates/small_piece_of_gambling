package io.betforge.player.model.restrictions

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.UUID

class CoolingOffPeriodRepository : CoolingOffPeriod.Repository() {

    fun getByPlayerId(playerId: UUID): CoolingOffPeriod {
        return EntryRestrictionsTable.select {
            EntryRestrictionsTable.playerId eq playerId and
                (EntryRestrictionsTable.type eq EntryRestrictions.Type.COOLING_OFF_PERIOD) and
                EntryRestrictionsTable.endedAt.greater(LocalDateTime.now())
        }.single().run { wrapRow(this) }
    }
}
