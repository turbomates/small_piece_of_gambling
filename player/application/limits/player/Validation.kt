package io.betforge.player.application.limits.player

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.domain.Currency
import io.betforge.player.application.limits.player.command.AddLimit
import io.betforge.player.application.limits.player.command.CancelLimit
import io.betforge.player.application.limits.player.command.EditLimit
import io.betforge.player.application.limits.player.queryobject.BetLimitCurrencyExistQO
import io.betforge.player.application.limits.player.queryobject.DepositLimitCurrencyExistQO
import io.betforge.player.application.limits.player.queryobject.LossLimitCurrencyExistQO
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import java.util.UUID

class Validation @Inject constructor(
    private val queryExecutor: QueryExecutor
) {
    object DepositLimitExist : Constraint {
        override val name: String
            get() = "The deposit limit already exists"
    }

    object LossLimitExist : Constraint {
        override val name: String
            get() = "The loss limit already exists"
    }

    object WagerLimitExist : Constraint {
        override val name: String
            get() = "The wager limit already exists"
    }

    object NotDepositLimitExist : Constraint {
        override val name: String
            get() = "The deposit limit does not exist"
    }

    object NotLossLimitExist : Constraint {
        override val name: String
            get() = "The loss limit does not exist"
    }

    object NotWagerLimitExist : Constraint {
        override val name: String
            get() = "The bet limit does not exist"
    }

    suspend fun onAddDepositLimit(command: AddLimit): List<Error> {
        return validate(command) {
            validate(AddLimit::playerId).isNotNull().isAddedCurrencyDepositLimit(command.money.currency)
            validate(AddLimit::money).isNotNull()
            validate(AddLimit::period).isNotNull()
        }
    }

    suspend fun onAddLossLimit(command: AddLimit): List<Error> {
        return validate(command) {
            validate(AddLimit::playerId).isNotNull().isAddedCurrencyLossLimit(command.money.currency)
            validate(AddLimit::money).isNotNull()
            validate(AddLimit::period).isNotNull()
        }
    }

    suspend fun onAddWagerLimit(command: AddLimit): List<Error> {
        return validate(command) {
            validate(AddLimit::playerId).isNotNull().isAddedCurrencyWagerLimit(command.money.currency)
            validate(AddLimit::money).isNotNull()
            validate(AddLimit::period).isNotNull()
        }
    }

    suspend fun onEditDepositLimit(command: EditLimit): List<Error> {
        return validate(command) {
            validate(EditLimit::playerId).isNotNull().isNotCurrencyDepositLimit(command.money.currency)
            validate(EditLimit::money).isNotNull()
        }
    }

    suspend fun onEditLossLimit(command: EditLimit): List<Error> {
        return validate(command) {
            validate(EditLimit::playerId).isNotNull().isNotCurrencyLossLimit(command.money.currency)
            validate(EditLimit::money).isNotNull()
        }
    }

    suspend fun onEditWagerLimit(command: EditLimit): List<Error> {
        return validate(command) {
            validate(EditLimit::playerId).isNotNull().isNotCurrencyWagerLimit(command.money.currency)
            validate(EditLimit::money).isNotNull()
        }
    }

    suspend fun onCancelWagerLimit(command: CancelLimit): List<Error> {
        return validate(command) {
            validate(CancelLimit::playerId).isNotNull().isNotCurrencyWagerLimit(command.currency)
            validate(CancelLimit::currency).isNotNull()
        }
    }

    suspend fun onCancelLossLimit(command: CancelLimit): List<Error> {
        return validate(command) {
            validate(CancelLimit::playerId).isNotNull().isNotCurrencyLossLimit(command.currency)
            validate(CancelLimit::currency).isNotNull()
        }
    }

    suspend fun onCancelDepositLimit(command: CancelLimit): List<Error> {
        return validate(command) {
            validate(CancelLimit::playerId).isNotNull().isNotCurrencyDepositLimit(command.currency)
            validate(CancelLimit::currency).isNotNull()
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isAddedCurrencyDepositLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(DepositLimitExist) { value ->
            value == null || !queryExecutor.execute(DepositLimitCurrencyExistQO(value, currency))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isAddedCurrencyLossLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(LossLimitExist) { value ->
            value == null || !queryExecutor.execute(LossLimitCurrencyExistQO(value, currency))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isAddedCurrencyWagerLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(WagerLimitExist) { value ->
            value == null || !queryExecutor.execute(BetLimitCurrencyExistQO(value, currency))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotCurrencyDepositLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(NotDepositLimitExist) { value ->
            value == null || queryExecutor.execute(DepositLimitCurrencyExistQO(value, currency))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotCurrencyLossLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(NotLossLimitExist) { value ->
            value == null || queryExecutor.execute(LossLimitCurrencyExistQO(value, currency))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotCurrencyWagerLimit(currency: Currency): Validator<E>.Property<UUID?> =
        this.coValidate(NotWagerLimitExist) { value ->
            value == null || queryExecutor.execute(BetLimitCurrencyExistQO(value, currency))
        }
}
