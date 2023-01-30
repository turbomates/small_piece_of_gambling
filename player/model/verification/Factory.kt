package io.betforge.player.model.verification

interface Factory {
    fun getVerifier(name: String): Verifier

    fun getVerifier(key: Verifier.Key<*>): Verifier
}
