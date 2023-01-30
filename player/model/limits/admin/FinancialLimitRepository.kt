package io.betforge.player.model.limits.admin

import io.betforge.player.infrasturcture.limits.RelationType
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import java.util.UUID

class FinancialLimitRepository : FinancialLimits.Repository() {
    fun isLimitExist(
        period: FinancialLimits.Period,
        relationType: RelationType,
        relationId: UUID?,
        type: FinancialLimits.Type
    ): Boolean {
        val (joinColumn, condition) = when (relationType) {
            RelationType.PLAYER -> AdminPlayersLimitsTable.limitId to
                relationId?.let { Op.build { AdminPlayersLimitsTable.playerId eq relationId } }
            RelationType.GROUP -> AdminGroupsLimitsTable.limitId to
                relationId?.let { Op.build { AdminGroupsLimitsTable.playerGroupId eq relationId } }
            RelationType.APPLICATION -> AdminApplicationLimitsTable.limitId to null
        }

        return !AdminFinancialLimitsTable
            .join(joinColumn.table, JoinType.LEFT, joinColumn, AdminFinancialLimitsTable.id)
            .select { AdminFinancialLimitsTable.period eq period and (AdminFinancialLimitsTable.type eq type) }
            .apply { condition?.let { andWhere { it } } }
            .empty()
    }
}
