package io.betforge.player.application.limits.admin

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.withDataBaseLock
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
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.limits.admin.DepositLimit
import io.betforge.player.model.limits.admin.FinancialLimitRepository
import io.betforge.player.model.limits.admin.FinancialLimits
import io.betforge.player.model.limits.admin.LossLimit
import io.betforge.player.model.limits.admin.MaxBetLimit
import io.betforge.player.model.limits.admin.MoneyLimit
import io.betforge.player.model.limits.admin.WagerLimit
import io.betforge.player.model.limits.admin.WithdrawLimit
import java.util.UUID

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    private val repository: FinancialLimitRepository
) {

    suspend fun handle(command: AddWagerLimit) {
        transaction {
            withDataBaseLock(command.relationId.hashCode()) {
                val limit = WagerLimit.create(
                    period = command.period,
                    relation = LimitRelation(command.relationId!!, command.relationType)
                )
                MoneyLimit.new(limit, command.amount)
            }
        }
    }

    suspend fun handle(command: AddLossLimit) {
        transaction {
            withDataBaseLock(command.relationId.hashCode()) {
                val limit = LossLimit.create(
                    period = command.period,
                    relation = LimitRelation(command.relationId!!, command.relationType)
                )
                MoneyLimit.new(limit, command.amount)
            }
        }
    }

    suspend fun handle(command: AddDepositLimit) {
        transaction {
            withDataBaseLock(command.relationId.hashCode()) {
                val limit = DepositLimit.create(
                    period = command.period,
                    relation = LimitRelation(command.relationId!!, command.relationType)
                )
                MoneyLimit.new(limit, command.amount)
            }
        }
    }

    suspend fun handle(command: AddWithdrawLimit) {
        transaction {
            withDataBaseLock(command.relationId.hashCode()) {
                val limit = WithdrawLimit.create(
                    period = command.period,
                    relation = LimitRelation(command.relationId!!, command.relationType)
                )
                MoneyLimit.new(limit, command.amount)
            }
        }
    }

    suspend fun handle(command: AddMaxBetLimit) {
        transaction {
            withDataBaseLock(command.relationId.hashCode()) {
                val limit = MaxBetLimit.create(LimitRelation(command.relationId!!, command.relationType))
                MoneyLimit.new(limit, command.amount)
            }
        }
    }

    suspend fun handle(command: EditWagerLimit) {
        transaction {
            withDataBaseLock(command.limitId.hashCode()) {
                repository[command.limitId].edit(command.amount, command.period)
            }
        }
    }

    suspend fun handle(command: EditLossLimit) {
        transaction {
            withDataBaseLock(command.limitId.hashCode()) {
                repository[command.limitId].edit(command.amount, command.period)
            }
        }
    }

    suspend fun handle(command: EditDepositLimit) {
        transaction {
            withDataBaseLock(command.limitId.hashCode()) {
                repository[command.limitId].edit(command.amount, command.period)
            }
        }
    }

    suspend fun handle(command: EditWithdrawLimit) {
        transaction {
            withDataBaseLock(command.limitId.hashCode()) {
                repository[command.limitId].edit(command.amount, command.period)
            }
        }
    }

    suspend fun handle(command: EditMaxBetLimit) {
        transaction {
            withDataBaseLock(command.limitId.hashCode()) {
                repository[command.limitId].edit(command.amount, FinancialLimits.Period.NONE)
            }
        }
    }

    suspend fun handleDeleteLimit(limitId: UUID) {
        transaction {
            withDataBaseLock(limitId.hashCode()) {
                repository.findById(limitId)?.remove()
            }
        }
    }
}
