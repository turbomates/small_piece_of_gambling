package io.betforge.player.application.restriction

import dev.tmsoft.lib.exposed.TransactionManager
import io.betforge.player.api.IDPath
import io.betforge.player.application.restriction.command.CancelCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateSelfExclusionPeriod
import io.betforge.player.model.restrictions.CoolingOffPeriod
import io.betforge.player.model.restrictions.CoolingOffPeriodRepository
import io.betforge.player.model.restrictions.SelfExclusionPeriod
import io.betforge.player.model.restrictions.SelfExclusionPeriodRepository
import javax.inject.Inject

class Handler @Inject constructor(
    private val transaction: TransactionManager,
    private val coolingOffPeriodRepository: CoolingOffPeriodRepository,
    private val selfExclusionPeriodRepository: SelfExclusionPeriodRepository
) {
    suspend fun createCoolingOffPeriod(
        command: CreateCoolingOffPeriod
    ) {
        transaction {
            CoolingOffPeriod.create(
                command.playerId,
                command.endedAt
            )
        }
    }

    suspend fun createSelfExclusionPeriod(
        command: CreateSelfExclusionPeriod
    ) {
        transaction {
            SelfExclusionPeriod.create(
                command.playerId,
                command.endedAt
            )
        }
    }

    suspend fun cancelCoolingOffPeriod(command: CancelCoolingOffPeriod) {
        transaction {
            val existCoolingOffPeriod = coolingOffPeriodRepository.getByPlayerId(command.playerId)
            existCoolingOffPeriod.cancel()
        }
    }

    suspend fun cancelSelfExclusionPeriod(params: IDPath) {
        transaction {
            val existSelfExclusionPeriod = selfExclusionPeriodRepository.getByPlayerId(params.id)
            existSelfExclusionPeriod.cancel()
        }
    }
}
