package io.betforge.player.application.filter

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.player.application.filter.command.AddFilter
import io.betforge.player.application.filter.command.DeleteFilter
import io.betforge.player.application.filter.command.EditFilter
import io.betforge.player.application.filter.command.UpdateCountFilter
import io.betforge.player.model.PlayerFilterTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class Handler @Inject constructor(
    private val transaction: TransactionManager
) {
    suspend fun addFilter(command: AddFilter) {
        transaction {
            PlayerFilterTable.insert {
                it[name] = command.name
                it[conditions] = command.conditions
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    suspend fun editFilter(command: EditFilter) {
        transaction {
            PlayerFilterTable.update({ PlayerFilterTable.name eq command.name }) {
                command.newName?.let { newName -> it[name] = newName }
                it[conditions] = command.conditions
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    suspend fun deleteFilter(command: DeleteFilter) {
        transaction {
            PlayerFilterTable.deleteWhere { PlayerFilterTable.name eq command.name }
        }
    }

    suspend fun updateCountFilter(command: UpdateCountFilter) {
        transaction {
            PlayerFilterTable.update({ PlayerFilterTable.id eq command.id }) {
                it[count] = command.count
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }
}
