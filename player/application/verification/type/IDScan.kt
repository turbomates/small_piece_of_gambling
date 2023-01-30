package io.betforge.player.application.verification.type

import com.google.inject.Inject
import io.betforge.player.model.verification.Status
import io.betforge.player.model.verification.Verification
import io.betforge.player.model.verification.VerificationData
import io.betforge.player.model.verification.VerificationInfo
import io.betforge.player.model.verification.Verifications
import io.betforge.player.model.verification.Verifier
import kotlinx.serialization.Serializable

class IDScan @Inject constructor(private val verifications: Verifications) : Verifier {
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
        info.idScan?.let {
            var uploadedPaths = emptyList<String>()
            if (verification.isHold()) {
                uploadedPaths = verification.lastData<IDScanData>()?.paths.orEmpty()
            }
            verification.appendData(IDScanData(it + uploadedPaths))
        }
        return verification
    }

    override suspend fun verify(info: VerificationInfo): Verification {
        return verifications.getByPlayerAndType(info.playerId, key)
    }

    companion object : Verifier.Key<IDScan>("id scan")
}

@Serializable
class IDScanData(
    val paths: List<String>,
    override val status: Status = Status.PENDING,
    override val reason: String? = null
) : VerificationData
