package io.betforge.player.model.limits.player

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.UUID

class WagerLimitRepository : WagerLimit.Repository() {

    fun getPlayerLimit(playerId: UUID, period: FinancialLimits.Period): WagerLimit {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.period eq period) and
                (FinancialLimitsTable.type eq FinancialLimits.Type.WAGER_LIMIT)
        }.single().run { wrapRow(this) }
    }

    fun findPlayerLimit(playerId: UUID, period: FinancialLimits.Period): WagerLimit? {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.period eq period) and
                (FinancialLimitsTable.type eq FinancialLimits.Type.WAGER_LIMIT)
        }.firstOrNull()?.let { wrapRow(it) }
    }

    fun getPlayerLimits(playerId: UUID): List<WagerLimit> {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.type eq FinancialLimits.Type.WAGER_LIMIT)
        }.map { wrapRow(it) }
    }
}
