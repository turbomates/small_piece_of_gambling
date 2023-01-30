package io.betforge.player.application.group.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.group.PlayerGroupsTable
import org.jetbrains.exposed.sql.select
import java.util.UUID

class SingleGroupQO(private val groupId: UUID) : QueryObject<Group?> {
    override suspend fun getData(): Group? {
        return PlayerGroupsTable
            .select { PlayerGroupsTable.id eq groupId }
            .singleOrNull()
            ?.toGroup()
    }
}
