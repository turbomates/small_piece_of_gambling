package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.structure.Either
import io.betforge.configuration.config.ApplicationConfiguration
import io.betforge.player.application.limits.admin.Handler
import io.betforge.player.application.limits.admin.Validation
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
import io.betforge.player.application.limits.admin.query.ApplicationLimitsQO
import io.betforge.player.application.limits.admin.query.FinancialLimit
import io.betforge.player.application.limits.admin.query.FinancialLimitQO
import io.betforge.player.application.limits.admin.query.GroupLimitsQO
import io.betforge.player.application.limits.admin.query.PlayerLimitsQO
import io.betforge.player.infrasturcture.limits.RelationType
import java.util.UUID

class AdminLimitsController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val application: ApplicationConfiguration,
    private val validation: Validation,
    private val handler: Handler
) : Controller() {
    suspend fun playerLimits(playerId: UUID): Response.Data<List<FinancialLimit>> {
        return Response.Data(queryExecutor.execute(PlayerLimitsQO(playerId)))
    }

    suspend fun groupLimits(playerGroupId: UUID): Response.Data<List<FinancialLimit>> {
        return Response.Data(queryExecutor.execute(GroupLimitsQO(playerGroupId)))
    }

    suspend fun applicationLimits(): Response.Data<List<FinancialLimit>> {
        return Response.Data(queryExecutor.execute(ApplicationLimitsQO(application.id)))
    }

    suspend fun show(limitId: UUID): Response.Data<FinancialLimit> {
        return Response.Data(queryExecutor.execute(FinancialLimitQO(limitId)))
    }

    suspend fun addWagerLimit(command: AddWagerLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        if (command.relationType == RelationType.APPLICATION) {
            command.relationId = application.id
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addLossLimit(command: AddLossLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        if (command.relationType == RelationType.APPLICATION) {
            command.relationId = application.id
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addDepositLimit(command: AddDepositLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        if (command.relationType == RelationType.APPLICATION) {
            command.relationId = application.id
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addMaxBetLimit(command: AddMaxBetLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddMaxBetLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        if (command.relationType == RelationType.APPLICATION) {
            command.relationId = application.id
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addWithdrawLimit(command: AddWithdrawLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddWithdrawLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        if (command.relationType == RelationType.APPLICATION) {
            command.relationId = application.id
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editWagerLimit(command: EditWagerLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editLossLimit(command: EditLossLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editDepositLimit(command: EditDepositLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editWithdrawLimit(command: EditWithdrawLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditWithdrawLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editMaxBetLimit(command: EditMaxBetLimit): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditMaxBetLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun deleteLimit(limitId: UUID): Response.Either<Response.Ok, Response.Errors> {
        handler.handleDeleteLimit(limitId)
        return Response.Either(Either.Left(Response.Ok))
    }
}
