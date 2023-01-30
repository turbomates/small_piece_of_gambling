package io.betforge.player.model.verification

import dev.tmsoft.lib.exposed.type.JsonBContains
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.and
import java.util.UUID

class VerificationRepository : Verification.Repository(), Verifications {
    override fun findByPlayer(playerId: UUID): List<Verification> {
        return find { VerificationTable.player eq playerId }.toList()
    }

    fun findByToken(token: UUID): Verification? {
        return find {
            JsonBContains(
                VerificationTable.data,
                VerificationTable.data.wrap(listOf(JsonObject(mapOf("token" to JsonPrimitive(token.toString())))))
            )
        }.singleOrNull()
    }

    override fun findByPlayerAndType(playerId: UUID, name: Verifier.Key<out Verifier>): Verification? {
        return find { VerificationTable.player eq playerId and (VerificationTable.type eq name.name) }.singleOrNull()
    }

    override fun getByPlayerAndType(playerId: UUID, name: Verifier.Key<out Verifier>): Verification {
        return find { VerificationTable.player eq playerId and (VerificationTable.type eq name.name) }.single()
    }
}
