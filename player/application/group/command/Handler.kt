package io.betforge.player.application.group.command

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.query.QueryExecutor
import io.betforge.player.application.PlayerIdsQO
import io.betforge.player.application.filter.queryobject.SingleFilterQO
import io.betforge.player.infrasturcture.group.GroupRepository
import io.betforge.player.model.group.Group

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    private val queryExecutor: QueryExecutor,
    private val repository: GroupRepository
) {
    suspend fun handle(command: CreateGroup) {
        transaction {
            val playerIds =
                if (command.playerIds == null && command.filterId != null) {
                    val filter = queryExecutor.execute(SingleFilterQO(command.filterId))
                    queryExecutor.execute(PlayerIdsQO(filter.conditions))
                } else command.playerIds!!

            Group.create(command.name, command.filterId, playerIds, command.color, command.priority)
        }
    }

    suspend fun handle(command: EditGroup) {
        transaction {
            val group = repository[command.id]
            group.edit(
                command.name,
                command.color,
                command.priority,
                command.filterId
            )
        }
    }

    suspend fun handle(command: DeleteGroup) {
        transaction {
            val group = repository[command.id]
            group.delete()
        }
    }

    suspend fun handle(command: AddPlayersToGroup) {
        transaction {
            val group = repository[command.groupId]
            group.addPlayers(command.playerIds.toSet())
        }
    }

    suspend fun handle(command: RemovePlayersFromGroup) {
        transaction {
            val group = repository[command.groupId]
            group.removePlayers(command.playerIds.toSet())
        }
    }

    suspend fun handle(command: UpdatePlayersInGroup) {
        transaction {
            val group = repository[command.groupId]
            group.updatePlayers(command.playerIds.toSet())
        }
    }
}
