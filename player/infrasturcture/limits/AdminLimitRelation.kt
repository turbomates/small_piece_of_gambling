package io.betforge.player.infrasturcture.limits

import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.model.limits.admin.AdminApplicationLimitsTable
import io.betforge.player.model.limits.admin.AdminGroupsLimitsTable
import io.betforge.player.model.limits.admin.AdminPlayersLimitsTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

@Serializable
data class LimitRelation(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val type: RelationType
)

enum class RelationType(val order: Int) {
    APPLICATION(1), GROUP(2), PLAYER(3)
}

fun ResultRow.toRelation(): RelationType {
    return when {
        this.getOrNull(AdminPlayersLimitsTable.playerId) != null -> RelationType.PLAYER
        this.getOrNull(AdminGroupsLimitsTable.playerGroupId) != null -> RelationType.GROUP
        this.getOrNull(AdminApplicationLimitsTable.application) != null -> RelationType.APPLICATION
        else -> throw IllegalArgumentException("Unknown relation type")
    }
}
