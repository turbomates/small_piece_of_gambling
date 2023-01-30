package io.betforge.player.application.verification

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.player.infrasturcture.PlayerRepository
import io.betforge.player.model.verification.Factory
import io.betforge.player.model.verification.RulesEngine
import io.betforge.player.model.verification.Verifications
import java.util.UUID

class Verification @Inject constructor(
    private val transaction: TransactionManager,
    private val rulesEngine: RulesEngine,
    private val verificationInfoLoader: VerificationInfoLoader,
    private val playerRepository: PlayerRepository,
    private val verificationFactory: Factory,
    private val verifications: Verifications
) {

    suspend fun start(playerId: UUID) {
        transaction {
            rulesEngine.init(verificationInfoLoader.load(playerId))
        }
    }

    suspend fun applyPlayerVerifications(playerId: UUID) {
        transaction {
            val checks = mutableMapOf<String, Boolean>()
            verifications.findByPlayer(playerId).filter { !it.isHold() }.forEach {
                checks[it.type] = it.isPassed()
            }
            rulesEngine.statusFor(checks)?.let {
                playerRepository[playerId].applyVerification(it)
            }
        }
    }

    suspend fun approve(playerId: UUID, key: String, reason: String? = null) {
        transaction {
            val verification = verificationFactory.getVerifier(key)
            val info = verificationInfoLoader.load(playerId)
            verification.approve(info, reason)
        }
    }

    suspend fun decline(playerId: UUID, key: String, reason: String? = null) {
        transaction {
            val verification = verificationFactory.getVerifier(key)
            val info = verificationInfoLoader.load(playerId)
            verification.decline(info, reason)
        }
    }
}
