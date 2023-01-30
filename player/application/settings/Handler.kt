package io.betforge.player.application.settings

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.upload.FileManagerFactory
import io.betforge.infrastructure.domain.Name
import io.betforge.player.application.settings.command.EditDetails
import io.betforge.player.application.settings.command.UpdateAvatar
import io.betforge.player.infrasturcture.PlayerRepository
import io.betforge.player.model.Details
import io.betforge.player.model.details.ContactDetails
import io.betforge.player.model.details.Gender
import io.betforge.player.model.details.Location
import io.betforge.player.model.details.PersonDetails

class Handler @Inject constructor(
    private val repository: PlayerRepository,
    private val transaction: TransactionManager,
    private val fileManager: FileManagerFactory
) {

    suspend fun handleEditDetails(command: EditDetails) {
        transaction {
            val player = repository[command.playerId]
            player.updateDetails(
                Details(
                    ContactDetails(command.mobile, command.phone),
                    Location(
                        command.country,
                        command.zip,
                        command.state,
                        command.city,
                        command.street,
                        command.house
                    ),
                    PersonDetails(
                        Name(command.firstName, command.lastName),
                        command.birthday,
                        command.gender?.let { Gender.valueOf(it) }
                    )
                )
            )
        }
    }

    suspend fun handleUpdateAvatar(command: UpdateAvatar) {
        transaction {
            val player = repository[command.playerId]
            player.uploadAvatar(command.avatar, fileManager.current())
        }
    }
}
