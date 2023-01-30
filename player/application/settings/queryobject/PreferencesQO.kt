package io.betforge.player.application.settings.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.serialization.LocaleSerializer
import io.betforge.identity.model.identity.UserTable
import io.betforge.infrastructure.domain.OddFormat
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import java.util.Locale
import java.util.UUID

class PreferencesQO(private val playerId: UUID) : QueryObject<Preferences> {
    override suspend fun getData(): Preferences {
        return UserTable
            .select { UserTable.id eq playerId }
            .first()
            .toPlayerPreferences()
    }
}

fun ResultRow.toPlayerPreferences() = Preferences(
    this[UserTable.preferences.locale],
    this[UserTable.preferences.oddFormat]
)

@Serializable
data class Preferences(
    @Serializable(with = LocaleSerializer::class)
    val locale: Locale?,
    val oddFormat: OddFormat?
)
