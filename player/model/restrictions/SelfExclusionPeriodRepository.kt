package io.betforge.player.model.restrictions

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.UUID

class SelfExclusionPeriodRepository : SelfExclusionPeriod.Repository() {

    fun getByPlayerId(playerId: UUID): SelfExclusionPeriod {
        return EntryRestrictionsTable
            .select {
                EntryRestrictionsTable.playerId eq playerId and
                    (EntryRestrictionsTable.type eq EntryRestrictions.Type.SELF_EXCLUSION_PERIOD) and
                    EntryRestrictionsTable.endedAt.greater(LocalDateTime.now())
            }
            .single()
            .run { wrapRow(this) }
    }
}
