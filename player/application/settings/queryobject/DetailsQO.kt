package io.betforge.player.application.settings.queryobject

import dev.tmsoft.lib.date.LocalDateSerializer
import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.player.model.PlayerTable
import io.betforge.player.model.details.Gender
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import java.time.LocalDate
import java.util.UUID

class DetailsQO(private val playerId: UUID) : QueryObject<Details> {
    override suspend fun getData(): Details {
        return PlayerTable
            .select { PlayerTable.id eq playerId }
            .first()
            .toPlayerDetails()
    }
}

fun ResultRow.toPlayerDetails(): Details {
    return Details(
        this[PlayerTable.details.personDetails.name.first],
        this[PlayerTable.details.personDetails.name.last],
        this[PlayerTable.details.personDetails.birthday],
        this[PlayerTable.details.personDetails.gender],
        this[PlayerTable.details.contactDetails.phone],
        this[PlayerTable.details.contactDetails.mobile],
        this[PlayerTable.details.location.country],
        this[PlayerTable.details.location.zip],
        this[PlayerTable.details.location.state],
        this[PlayerTable.details.location.city],
        this[PlayerTable.details.location.street],
        this[PlayerTable.details.location.house]
    )
}

@Serializable
data class Details(
    val firstName: String?,
    val lastName: String?,
    @Serializable(with = LocalDateSerializer::class)
    val birthday: LocalDate?,
    val gender: Gender?,
    val phone: String?,
    val mobile: String?,
    val country: String?,
    val zip: String?,
    val state: String?,
    val city: String?,
    val street: String?,
    val house: String?
)
