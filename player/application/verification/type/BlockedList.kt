package io.betforge.player.application.verification.type

import com.google.inject.Inject
import io.betforge.player.model.verification.Status
import io.betforge.player.model.verification.Verification
import io.betforge.player.model.verification.VerificationData
import io.betforge.player.model.verification.VerificationInfo
import io.betforge.player.model.verification.Verifications
import io.betforge.player.model.verification.Verifier
import kotlinx.serialization.Serializable

class BlockedList @Inject constructor(private val verifications: Verifications) : Verifier {
    override val key = Companion

    override fun approve(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.approve(reason)
        return verification
    }

    override suspend fun init(info: VerificationInfo): Verification {
        val verification = verifications.findByPlayerAndType(info.playerId, key)
            ?: Verification.create(info.playerId, key)
        verification.appendData(BlockedListData(info.email))
        verification.approve("not blocked")
        return verification
    }

    override fun decline(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.decline(reason)
        return verification
    }

    override suspend fun verify(info: VerificationInfo): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.approve()
        return verification
    }

    companion object : Verifier.Key<BlockedList>("blocked")
}

@Serializable
data class BlockedListData(
    val email: String,
    override val status: Status = Status.PENDING,
    override val reason: String? = null
) : VerificationData
