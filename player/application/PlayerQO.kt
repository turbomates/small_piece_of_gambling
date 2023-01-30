package io.betforge.player.application

import dev.tmsoft.lib.date.LocalDateSerializer
import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.query.filter.PathValues
import dev.tmsoft.lib.query.filter.filter
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousListBuilder
import dev.tmsoft.lib.serialization.LocaleSerializer
import dev.tmsoft.lib.serialization.UUIDSerializer
import dev.tmsoft.lib.upload.FileManager
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.infrastructure.domain.OddFormat
import io.betforge.player.application.filter.condition.Condition
import io.betforge.player.application.filter.condition.applyConditions
import io.betforge.player.model.PlayerTable
import io.betforge.player.model.Status
import io.betforge.player.model.details.Gender
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

class PlayerIdsQO(private val conditions: List<Condition>) : QueryObject<List<UUID>> {
    override suspend fun getData(): List<UUID> {
        return PlayerTable
            .slice(PlayerTable.id)
            .selectAll()
            .applyConditions(conditions)
            .map { it[PlayerTable.id].value }
    }
}

class PlayersQO(
    private val paging: PagingParameters,
    private val fileManager: FileManager,
    private val filterValues: PathValues = PathValues()
) :
    QueryObject<ContinuousList<Player>> {
    override suspend fun getData(): ContinuousList<Player> {
        return PlayerTable
            .join(UserTable, JoinType.INNER, UserTable.id, PlayerTable.id)
            .join(UsernamePasswordCredentialsTable, JoinType.LEFT, UsernamePasswordCredentialsTable.id, PlayerTable.id)
            .selectAll()
            .filter(PlayersFilter, filterValues)
            .orderBy(UserTable.createdAt, SortOrder.DESC)
            .toContinuousListBuilder(paging, null, true) { this.map { it.toPlayer(fileManager) } }
    }
}

class PlayerQO(private val playerId: UUID, private val fileManager: FileManager) : QueryObject<Player> {
    override suspend fun getData(): Player {
        return PlayerTable
            .join(UserTable, JoinType.INNER, UserTable.id, PlayerTable.id)
            .join(UsernamePasswordCredentialsTable, JoinType.LEFT, UsernamePasswordCredentialsTable.id, PlayerTable.id)
            .select { PlayerTable.id eq playerId }
            .first()
            .toPlayer(fileManager)
    }
}

class PlayerStatusQO(private val playerId: UUID) : QueryObject<Status?> {
    override suspend fun getData(): Status? {
        return PlayerTable
            .slice(PlayerTable.status)
            .select { PlayerTable.id eq playerId }
            .firstOrNull()
            ?.let { it[PlayerTable.status] }
    }
}

class PlayerExistsQO(private val playerId: UUID) : QueryObject<Boolean> {
    override suspend fun getData(): Boolean {
        return !PlayerTable
            .select { PlayerTable.id eq playerId }
            .empty()
    }
}

fun ResultRow.toPlayer(fileManager: FileManager): Player {
    return Player(
        this[PlayerTable.id].value,
        this[UserTable.username],
        this[UsernamePasswordCredentialsTable.email.address],
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
        this[PlayerTable.details.location.house],
        this[UserTable.preferences.locale],
        this[UserTable.preferences.oddFormat],
        this[PlayerTable.avatar]?.let { fileManager.getWebUri(it) },
        this[PlayerTable.status],
        this[PlayerTable.createdAt]
    )
}

@Serializable
data class Player(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val email: String?,
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
    val house: String?,
    @Serializable(with = LocaleSerializer::class)
    val locale: Locale?,
    val oddFormat: OddFormat?,
    val avatar: String?,
    val status: Status,
    @Serializable(with = LocalDateTimeSerializer::class)
    val joinedAt: LocalDateTime
)
