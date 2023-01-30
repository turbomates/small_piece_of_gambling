package io.betforge.player.model.verification

import io.betforge.player.model.Status
import java.util.UUID

data class Rule(val status: Status, val conditions: String)

interface RulesLoader {
    fun rules(): List<Rule>
}

interface Verifications {
    fun findByPlayerAndType(playerId: UUID, name: Verifier.Key<out Verifier>): Verification?
    fun findByPlayer(playerId: UUID): List<Verification>
    fun getByPlayerAndType(playerId: UUID, name: Verifier.Key<out Verifier>): Verification
}
