package io.betforge.player.application.verification.type

import com.google.inject.Inject
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.model.verification.Status
import io.betforge.player.model.verification.Verification
import io.betforge.player.model.verification.VerificationData
import io.betforge.player.model.verification.VerificationInfo
import io.betforge.player.model.verification.Verifications
import io.betforge.player.model.verification.Verifier
import kotlinx.serialization.Serializable
import java.util.UUID

class Email @Inject constructor(private val verifications: Verifications) : Verifier {
    override val key = Companion

    override fun decline(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.decline(reason)
        return verification
    }

    override fun approve(info: VerificationInfo, reason: String?): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        verification.approve(reason)
        return verification
    }

    override suspend fun init(info: VerificationInfo): Verification {
        val verification = verifications.findByPlayerAndType(info.playerId, key)
            ?: Verification.create(info.playerId, key)
        verification.appendData(EmailData(info.email, UUID.randomUUID()))
        return verification
    }

    override suspend fun verify(info: VerificationInfo): Verification {
        val verification = verifications.getByPlayerAndType(info.playerId, key)
        val data = verification.lastData<EmailData>()
        val requestData = EmailData(info.email, info.emailCode ?: UUID.randomUUID())
        if (requestData.token == data?.token && requestData.email == data.email) {
            verification.approve()
        } else {
            verification.decline()
        }
        return verification
    }

    companion object : Verifier.Key<Email>("email")
}

@Serializable
data class EmailData(
    val email: String,
    @Serializable(with = UUIDSerializer::class) val token: UUID,
    override val status: Status = Status.PENDING,
    override val reason: String? = null
) : VerificationData
