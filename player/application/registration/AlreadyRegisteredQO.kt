package io.betforge.player.application.registration

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.PlayerTable
import org.jetbrains.exposed.sql.select
import java.util.UUID

class AlreadyRegisteredQO(private val userId: UUID) : QueryObject<Boolean> {
    override suspend fun getData(): Boolean {
        return PlayerTable
            .select { PlayerTable.id eq userId }
            .singleOrNull() != null
    }
}
