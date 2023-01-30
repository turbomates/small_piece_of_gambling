package io.betforge.player.application.limits.player

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.TransactionManager
import dev.tmsoft.lib.exposed.withDataBaseLock
import io.betforge.player.application.limits.player.command.AddLimit
import io.betforge.player.application.limits.player.command.CancelLimit
import io.betforge.player.application.limits.player.command.EditLimit
import io.betforge.player.model.limits.player.DepositLimit
import io.betforge.player.model.limits.player.DepositLimitRepository
import io.betforge.player.model.limits.player.LossLimit
import io.betforge.player.model.limits.player.LossLimitRepository
import io.betforge.player.model.limits.player.WagerLimit
import io.betforge.player.model.limits.player.WagerLimitRepository
import java.util.UUID

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    private val depositLimitRepository: DepositLimitRepository,
    private val lossLimitRepository: LossLimitRepository,
    private val wagerLimitRepository: WagerLimitRepository,
) {
    suspend fun addDepositLimit(
        command: AddLimit
    ) {
        transaction {
            withDataBaseLock(command.playerId.hashCode()) {
                val existLimit = depositLimitRepository.findPlayerLimit(command.playerId, command.period)
                if (existLimit == null) {
                    val newLimit = DepositLimit.create(command.playerId, command.period)
                    newLimit.add(command.money)
                } else {
                    existLimit.add(command.money)
                }
            }
        }
    }

    suspend fun addLossLimit(
        command: AddLimit
    ) {
        transaction {
            withDataBaseLock(command.playerId.hashCode()) {
                val existLimit = lossLimitRepository.findPlayerLimit(command.playerId, command.period)
                if (existLimit == null) {
                    val limit = LossLimit.create(command.playerId, command.period)
                    limit.add(command.money)
                } else {
                    existLimit.add(command.money)
                }
            }
        }
    }

    suspend fun addBetLimit(
        command: AddLimit
    ) {
        transaction {
            withDataBaseLock(command.playerId.hashCode()) {
                val existLimit = wagerLimitRepository.findPlayerLimit(command.playerId, command.period)
                if (existLimit == null) {
                    val newLimit = WagerLimit.create(command.playerId, command.period)
                    newLimit.add(command.money)
                } else {
                    existLimit.add(command.money)
                }
            }
        }
    }

    suspend fun editDepositLimit(
        command: EditLimit
    ) {
        transaction {
            val existLimit = depositLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.edit(command.money)
        }
    }

    suspend fun editLossLimit(
        command: EditLimit
    ) {
        transaction {
            val existLimit = lossLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.edit(command.money)
        }
    }

    suspend fun editWagerLimit(
        command: EditLimit
    ) {
        transaction {
            val existLimit = wagerLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.edit(command.money)
        }
    }

    suspend fun cancelDepositLimit(
        actor: UUID,
        command: CancelLimit
    ) {
        transaction {
            val existLimit = depositLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.currency, actor)
        }
    }

    suspend fun cancelLossLimit(
        actor: UUID,
        command: CancelLimit
    ) {
        transaction {
            val existLimit = lossLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.currency, actor)
        }
    }

    suspend fun cancelWagerLimit(
        actor: UUID,
        command: CancelLimit
    ) {
        transaction {
            val existLimit = wagerLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.currency, actor)
        }
    }

    // TODO add cancel and add in one method in MoneyLimit Model
    suspend fun adminEditDepositLimit(
        actor: UUID,
        command: EditLimit
    ) {
        transaction {
            val existLimit = depositLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.money.currency, actor)
            existLimit.edit(command.money)
        }
    }

    // TODO add cancel and add in one method in MoneyLimit Model
    suspend fun adminEditLossLimit(
        actor: UUID,
        command: EditLimit
    ) {
        transaction {
            val existLimit = lossLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.money.currency, actor)
            existLimit.edit(command.money)
        }
    }

    // TODO add cancel and add in one method in MoneyLimit Model
    suspend fun adminEditBetLimit(
        actor: UUID,
        command: EditLimit
    ) {
        transaction {
            val existLimit = wagerLimitRepository.getPlayerLimit(command.playerId, command.period)
            existLimit.cancel(command.money.currency, actor)
            existLimit.edit(command.money)
        }
    }
}
