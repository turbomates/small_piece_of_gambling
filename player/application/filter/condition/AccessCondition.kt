package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.validation.Error
import io.betforge.player.model.PlayerTable
import io.betforge.player.model.restrictions.EntryRestrictions
import io.betforge.player.model.restrictions.EntryRestrictionsTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.util.UUID

@Serializable
@SerialName("access_condition")
class AccessCondition(
    private val accessLimit: Boolean,
    private val limitType: EntryRestrictions.Type
) : StrictCondition {
    override suspend fun validate(): List<Error> = emptyList()

    override fun strictIds(): Set<UUID> {
        return PlayerTable
            .join(EntryRestrictionsTable, JoinType.LEFT, EntryRestrictionsTable.playerId, PlayerTable.id)
            .select {
                when (accessLimit) {
                    true -> EntryRestrictionsTable.type eq limitType
                    false -> EntryRestrictionsTable.type neq limitType or EntryRestrictionsTable.playerId.isNull()
                }
            }
            .map { it[PlayerTable.id].value }
            .toSet()
    }
}
