package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.model.verification.Status
import io.betforge.player.model.verification.VerificationTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Verification(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val player: UUID,
    val type: String,
    val status: Status,
    val reason: String?,
    val data: List<JsonElement>,
    @Serializable(with = LocalDateTimeSerializer::class) val changedAt: LocalDateTime,
    val username: String? = null,
    val email: String? = null
)

fun ResultRow.toVerification(): Verification {
    return Verification(
        this[VerificationTable.id].value,
        this[VerificationTable.player],
        this[VerificationTable.type],
        this[VerificationTable.status],
        this[VerificationTable.reason],
        this[VerificationTable.data],
        this[VerificationTable.changedAt],
        this.getOrNull(UserTable.username),
        this.getOrNull(UsernamePasswordCredentialsTable.email)?.address,
    )
}
