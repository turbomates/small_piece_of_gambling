package io.betforge.player.model.limits.player

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.UUID

class LossLimitRepository : LossLimit.Repository() {

    fun getPlayerLimit(playerId: UUID, period: FinancialLimits.Period): LossLimit {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.period eq period) and
                (FinancialLimitsTable.type eq FinancialLimits.Type.LOSS_LIMIT)
        }.single().run { wrapRow(this) }
    }

    fun findPlayerLimit(playerId: UUID, period: FinancialLimits.Period): LossLimit? {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.period eq period) and
                (FinancialLimitsTable.type eq FinancialLimits.Type.LOSS_LIMIT)
        }.firstOrNull()?.let { wrapRow(it) }
    }

    fun getPlayerLimits(playerId: UUID): List<LossLimit> {
        return FinancialLimitsTable.select {
            FinancialLimitsTable.playerId eq playerId and
                (FinancialLimitsTable.type eq FinancialLimits.Type.LOSS_LIMIT)
        }.map { wrapRow(it) }
    }
}
