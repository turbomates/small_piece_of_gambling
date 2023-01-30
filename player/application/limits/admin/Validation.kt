package io.betforge.player.application.limits.admin

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.domain.Money
import io.betforge.player.application.PlayerExistsQO
import io.betforge.player.application.group.queryobject.GroupExistsQO
import io.betforge.player.application.limits.admin.command.AddDepositLimit
import io.betforge.player.application.limits.admin.command.AddLossLimit
import io.betforge.player.application.limits.admin.command.AddMaxBetLimit
import io.betforge.player.application.limits.admin.command.AddWagerLimit
import io.betforge.player.application.limits.admin.command.AddWithdrawLimit
import io.betforge.player.application.limits.admin.command.EditDepositLimit
import io.betforge.player.application.limits.admin.command.EditLossLimit
import io.betforge.player.application.limits.admin.command.EditMaxBetLimit
import io.betforge.player.application.limits.admin.command.EditWagerLimit
import io.betforge.player.application.limits.admin.command.EditWithdrawLimit
import io.betforge.player.infrasturcture.limits.RelationType
import io.betforge.player.model.limits.admin.FinancialLimitRepository
import io.betforge.player.model.limits.admin.FinancialLimits
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositive
import org.valiktor.functions.isValid
import org.valiktor.functions.validate
import java.util.UUID

class Validation @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val transaction: TransactionManager,
    private val limitRepository: FinancialLimitRepository
) {
    suspend fun onAddWagerLimit(command: AddWagerLimit): List<Error> {
        return validate(command) {
            validate(AddWagerLimit::relationType).isNewLimit(
                command.period,
                command.relationId,
                FinancialLimits.Type.WAGER_LIMIT
            )
            validate(AddWagerLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(AddWagerLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
            if (command.relationType != RelationType.APPLICATION) {
                validate(AddWagerLimit::relationId).isNotNull().isRelationExist(command.relationType)
            }
        }
    }

    suspend fun onAddLossLimit(command: AddLossLimit): List<Error> {
        return validate(command) {
            validate(AddLossLimit::relationType).isNewLimit(
                command.period,
                command.relationId,
                FinancialLimits.Type.LOSS_LIMIT
            )
            validate(AddLossLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(AddLossLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
            if (command.relationType != RelationType.APPLICATION) {
                validate(AddLossLimit::relationId).isNotNull().isRelationExist(command.relationType)
            }
        }
    }

    suspend fun onAddDepositLimit(command: AddDepositLimit): List<Error> {
        return validate(command) {
            validate(AddDepositLimit::relationType).isNewLimit(
                command.period,
                command.relationId,
                FinancialLimits.Type.DEPOSIT_LIMIT
            )
            validate(AddDepositLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(AddDepositLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
            if (command.relationType != RelationType.APPLICATION) {
                validate(AddDepositLimit::relationId).isNotNull().isRelationExist(command.relationType)
            }
        }
    }

    suspend fun onAddWithdrawLimit(command: AddWithdrawLimit): List<Error> {
        return validate(command) {
            validate(AddWithdrawLimit::relationType).isNewLimit(
                command.period,
                command.relationId,
                FinancialLimits.Type.WITHDRAW_LIMIT
            )
            validate(AddWithdrawLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(AddWithdrawLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
            if (command.relationType != RelationType.APPLICATION) {
                validate(AddWithdrawLimit::relationId).isNotNull().isRelationExist(command.relationType)
            }
        }
    }

    suspend fun onAddMaxBetLimit(command: AddMaxBetLimit): List<Error> {
        return validate(command) {
            validate(AddMaxBetLimit::relationType).isNewLimit(
                FinancialLimits.Period.NONE,
                command.relationId,
                FinancialLimits.Type.MAX_BET_LIMIT
            )
            validate(AddMaxBetLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
            if (command.relationType != RelationType.APPLICATION) {
                validate(AddMaxBetLimit::relationId).isNotNull().isRelationExist(command.relationType)
            }
        }
    }

    suspend fun onEditWagerLimit(command: EditWagerLimit): List<Error> {
        return validate(command) {
            validate(EditWagerLimit::limitId).isLimitExist()
            validate(EditWagerLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(EditWagerLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
        }
    }

    suspend fun onEditLossLimit(command: EditLossLimit): List<Error> {
        return validate(command) {
            validate(EditLossLimit::limitId).isLimitExist()
            validate(EditLossLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(EditLossLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
        }
    }

    suspend fun onEditDepositLimit(command: EditDepositLimit): List<Error> {
        return validate(command) {
            validate(EditDepositLimit::limitId).isLimitExist()
            validate(EditDepositLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(EditDepositLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
        }
    }

    suspend fun onEditWithdrawLimit(command: EditWithdrawLimit): List<Error> {
        return validate(command) {
            validate(EditWithdrawLimit::limitId).isLimitExist()
            validate(EditWithdrawLimit::period).isValid { field -> field != FinancialLimits.Period.NONE }
            validate(EditWithdrawLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
        }
    }

    suspend fun onEditMaxBetLimit(command: EditMaxBetLimit): List<Error> {
        return validate(command) {
            validate(EditMaxBetLimit::limitId).isLimitExist()
            validate(EditMaxBetLimit::amount).validate {
                validate(Money::amount).isPositive()
                validate(Money::currency).validate(CorrectCurrency) { field -> field == Currency.POINT }
            }
        }
    }

    object LimitNotExist : Constraint {
        override val name: String
            get() = "The admin limit already exists"
    }

    object LimitExist : Constraint {
        override val name: String
            get() = "The admin limit doesn't exist"
    }

    object CorrectCurrency : Constraint {
        override val name: String
            get() = "Should be a POINT currency"
    }

    private suspend fun <E> Validator<E>.Property<RelationType?>.isNewLimit(
        period: FinancialLimits.Period,
        relationId: UUID?,
        type: FinancialLimits.Type
    ): Validator<E>.Property<RelationType?> = this.coValidate(LimitNotExist) { value ->
        transaction {
            value != null && !limitRepository.isLimitExist(period, value, relationId, type)
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isLimitExist():
        Validator<E>.Property<UUID?> = this.coValidate(LimitExist) { value ->
        transaction {
            value != null && limitRepository.findById(value) != null
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isRelationExist(relationType: RelationType):
        Validator<E>.Property<UUID?> = this.coValidate(LimitExist) { value ->
        value != null && when (relationType) {
            RelationType.PLAYER -> queryExecutor.execute(PlayerExistsQO(value))
            RelationType.GROUP -> queryExecutor.execute(GroupExistsQO(value))
            RelationType.APPLICATION -> false
        }
    }
}
