package io.betforge.player.application.verification

import com.google.inject.Injector
import io.betforge.player.application.verification.type.BlockedList
import io.betforge.player.application.verification.type.Email
import io.betforge.player.application.verification.type.GameStop
import io.betforge.player.application.verification.type.IDScan
import io.betforge.player.model.verification.Factory
import io.betforge.player.model.verification.Verifier

class VerificationFactory(private val injector: Injector) : Factory {
    private val map = mapOf(
        getVerifierClass(BlockedList),
        getVerifierClass(Email),
        getVerifierClass(IDScan),
        getVerifierClass(GameStop)
    )

    private inline fun <reified T : Verifier> getVerifierClass(key: Verifier.Key<T>): Pair<String, Class<T>> {
        return key.name to T::class.java
    }

    override fun getVerifier(name: String): Verifier {
        val clazz = map[name] ?: throw VerificationNotFound("Verifier not found for key '$name'")
        return injector.getInstance(clazz)
    }

    override fun getVerifier(key: Verifier.Key<*>): Verifier {
        val clazz = map[key.name] ?: throw VerificationNotFound("Verifier not found for key '${key.name}'")
        return injector.getInstance(clazz)
    }
}

class VerificationNotFound(message: String) : Exception(message)
