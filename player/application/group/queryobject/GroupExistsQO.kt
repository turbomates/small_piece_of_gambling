package io.betforge.player.application.group.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.group.PlayerGroupsTable
import org.jetbrains.exposed.sql.select
import java.util.UUID

class GroupExistsQO(private val groupId: UUID) : QueryObject<Boolean> {
    override suspend fun getData(): Boolean {
        return PlayerGroupsTable
            .select { PlayerGroupsTable.id eq groupId }
            .singleOrNull() != null
    }
}
