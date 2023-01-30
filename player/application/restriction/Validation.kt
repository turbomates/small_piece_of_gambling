package io.betforge.player.application.restriction

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.player.api.IDPath
import io.betforge.player.application.restriction.command.CancelCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateSelfExclusionPeriod
import io.betforge.player.application.restriction.queryobject.CoolingOffPeriodExistQO
import io.betforge.player.application.restriction.queryobject.SelfExclusionPeriodExistQO
import org.valiktor.Constraint
import org.valiktor.Validator
import org.valiktor.functions.isNotNull
import java.util.UUID

class Validation @Inject constructor(
    private val queryExecutor: QueryExecutor
) {
    object CoolingOffPeriodExist : Constraint {
        override val name: String
            get() = "The cooling off period already exists"
    }

    object SelfExclusionPeriodExist : Constraint {
        override val name: String
            get() = "The self exclusion period exists"
    }

    object NotSelfExclusionExist : Constraint {
        override val name: String
            get() = "The self exclusion period not exists"
    }

    object NotCoolingOffPeriodExist : Constraint {
        override val name: String
            get() = "The cooling off period not exists"
    }

    suspend fun onSelfExclusionPeriod(command: CreateSelfExclusionPeriod): List<Error> {
        return validate(command) {
            validate(CreateSelfExclusionPeriod::playerId).isNotNull().isSelfExclusionPeriod()
            validate(CreateSelfExclusionPeriod::endedAt).isNotNull()
        }
    }

    suspend fun onCoolingOffPeriod(command: CreateCoolingOffPeriod): List<Error> {
        return validate(command) {
            validate(CreateCoolingOffPeriod::playerId).isNotNull().isCoolingOffPeriod()
            validate(CreateCoolingOffPeriod::endedAt).isNotNull()
        }
    }

    suspend fun onCancelCoolingOffPeriod(command: CancelCoolingOffPeriod): List<Error> {
        return validate(command) {
            validate(CancelCoolingOffPeriod::playerId).isNotNull().isNotCoolingOffPeriod()
        }
    }

    suspend fun onCancelSelfExclusionPeriod(params: IDPath): List<Error> {
        return validate(params) {
            validate(IDPath::id).isNotNull().isNotSelfExclusionPeriod()
        }
    }

    private suspend fun <E> Validator<E>.Property<UUID?>.isCoolingOffPeriod(): Validator<E>.Property<UUID?> =
        this.coValidate(CoolingOffPeriodExist) { value ->
            value == null || !queryExecutor.execute(CoolingOffPeriodExistQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isSelfExclusionPeriod(): Validator<E>.Property<UUID?> =
        this.coValidate(SelfExclusionPeriodExist) { value ->
            value == null || !queryExecutor.execute(SelfExclusionPeriodExistQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotSelfExclusionPeriod(): Validator<E>.Property<UUID?> =
        this.coValidate(NotSelfExclusionExist) { value ->
            value == null || queryExecutor.execute(SelfExclusionPeriodExistQO(value))
        }

    private suspend fun <E> Validator<E>.Property<UUID?>.isNotCoolingOffPeriod(): Validator<E>.Property<UUID?> =
        this.coValidate(NotCoolingOffPeriodExist) { value ->
            value == null || queryExecutor.execute(CoolingOffPeriodExistQO(value))
        }
}
