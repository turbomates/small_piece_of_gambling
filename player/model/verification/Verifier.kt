package io.betforge.player.model.verification

import java.util.UUID

interface Verifier {
    val key: Key<*>
    suspend fun verify(info: VerificationInfo): Verification
    fun decline(info: VerificationInfo, reason: String?): Verification
    fun approve(info: VerificationInfo, reason: String?): Verification
    suspend fun init(info: VerificationInfo): Verification
    abstract class Key<T : Verifier>(val name: String) {
        override fun toString(): String = name.ifEmpty { super.toString() }
    }
}

interface VerificationInfoLoader {
    suspend fun load(playerId: UUID): VerificationInfo
}

data class VerificationInfo(
    val playerId: UUID,
    val bio: Bio,
    val address: Address,
    val email: String,
    val emailCode: UUID? = null,
    val idScan: List<String>? = null
)

data class Bio(val firstName: String, val lastName: String, val birthday: String)
data class Address(val postcode: String)
