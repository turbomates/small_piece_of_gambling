package io.betforge.player.application.registration

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.validation.Unique
import io.betforge.infrastructure.extensions.validation.isApplicationCurrency
import io.betforge.player.application.isGender
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import java.util.UUID
import javax.annotation.Nullable
import io.betforge.configuration.model.apps.Wallet as WalletConfig

class Validation @Inject constructor(
    @Nullable private val wallet: WalletConfig?,
    private val queryExecutor: QueryExecutor
) {

    suspend fun onRegisterEuropean(command: European): List<Error> {
        return validate(command) {
            validate(European::userId).isNotNull().isNotRegisteredUser()
            validate(European::firstName).isNotNull()
            validate(European::lastName).isNotNull()
            validate(European::birthday).isNotNull()
            validate(European::gender).isNotNull().isGender()
            validate(European::zip).isNotNull()
            validate(European::country).isNotNull()
            validate(European::state).isNotNull()
            validate(European::city).isNotNull()
            validate(European::street).isNotNull()
            validate(European::house).isNotNull()
            validate(European::phone).isNotNull()
            validate(European::mobile).isNotNull()
            validate(European::currency).isNotNull().isApplicationCurrency(wallet)
        }
    }

    suspend fun onRegisterSimple(command: Simple): List<Error> {
        return validate(command) {
            validate(Simple::userId).isNotNull().isNotRegisteredUser()
        }
    }

    suspend fun onRegisterUK(command: UK): List<Error> {
        return validate(command) {
            validate(UK::userId).isNotNull().isNotRegisteredUser()
            validate(UK::firstName).isNotNull()
            validate(UK::lastName).isNotNull()
            validate(UK::birthday).isNotNull()
            validate(UK::gender).isNotNull().isGender()
            validate(UK::zip).isNotNull()
            validate(UK::country).isNotNull()
            validate(UK::state).isNotNull()
            validate(UK::city).isNotNull()
            validate(UK::street).isNotNull()
            validate(UK::house).isNotNull()
            validate(UK::phone).isNotNull()
            validate(UK::mobile).isNotNull()
        }
    }

    suspend fun onRegisterTelegram(command: Telegram): List<Error> {
        return validate(command) {
            validate(Telegram::userId).isNotNull()
            validate(Telegram::firstName).isNotNull()
            validate(Telegram::lastName).isNotNull()
            validate(Telegram::currency).isNotNull().isApplicationCurrency(wallet)
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotRegisteredUser(): Validator<E>.Property<UUID?> =
        this.coValidate(Unique) { value ->
            value == null || !queryExecutor.execute(AlreadyRegisteredQO(value))
        }
}
