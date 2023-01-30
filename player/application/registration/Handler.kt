package io.betforge.player.application.registration

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.infrastructure.domain.Name
import io.betforge.player.model.Details
import io.betforge.player.model.Player
import io.betforge.player.model.details.ContactDetails
import io.betforge.player.model.details.Gender
import io.betforge.player.model.details.Location
import io.betforge.player.model.details.PersonDetails
import javax.annotation.Nullable
import io.betforge.configuration.model.apps.Player as PlayerConfig

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    @Nullable private val player: PlayerConfig?
) {

    suspend fun handleRegister(command: European) {
        transaction {
            Player.register(
                command.userId,
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
                        Gender.valueOf(command.gender.uppercase())
                    )
                ),
                player?.registrationStatus
            )
        }
    }

    suspend fun handleRegisterSimple(command: Simple) {
        transaction {
            Player.register(
                command.userId,
                null,
                player?.registrationStatus
            )
        }
    }

    suspend fun handleRegisterUK(command: UK) {
        transaction {
            Player.register(
                command.userId,
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
                        Gender.valueOf(command.gender.uppercase())
                    )
                ),
                player?.registrationStatus
            )
        }
    }

    suspend fun handleRegisterTelegram(command: Telegram) {
        transaction {
            Player.register(
                command.userId,
                Details(
                    personDetails = PersonDetails(
                        Name(command.firstName, command.lastName)
                    )
                ),
                player?.registrationStatus
            )
        }
    }
}
