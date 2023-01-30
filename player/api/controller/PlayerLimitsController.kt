package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.limits.player.Handler
import io.betforge.player.application.limits.player.Validation
import io.betforge.player.application.limits.player.command.AddLimit
import io.betforge.player.application.limits.player.command.CancelLimit
import io.betforge.player.application.limits.player.command.EditLimit
import io.betforge.player.application.limits.player.queryobject.FinancialLimits
import io.betforge.player.application.limits.player.queryobject.FinancialLimitsQO
import java.util.UUID

class PlayerLimitsController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val handler: Handler,
    private val validation: Validation
) : Controller() {

    suspend fun addDepositLimit(
        playerId: UUID,
        command: AddLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onAddDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.addDepositLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addLossLimit(
        playerId: UUID,
        command: AddLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onAddLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.addLossLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addWagerLimit(
        playerId: UUID,
        command: AddLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onAddWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.addBetLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editDepositLimit(
        playerId: UUID,
        command: EditLimit,
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.editDepositLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editLossLimit(
        playerId: UUID,
        command: EditLimit,
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.editLossLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun editWagerLimit(
        playerId: UUID,
        command: EditLimit,
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.editWagerLimit(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun cancelDepositLimit(
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelDepositLimit(playerId, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun cancelLossLimit(
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelLossLimit(playerId, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun cancelWagerLimit(
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelWagerLimit(playerId, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminEditDepositLimit(
        actor: UUID,
        playerId: UUID,
        command: EditLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.adminEditDepositLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminEditLossLimit(
        actor: UUID,
        playerId: UUID,
        command: EditLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.adminEditLossLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminEditWagerLimit(
        actor: UUID,
        playerId: UUID,
        command: EditLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.adminEditBetLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminCancelDepositLimit(
        actor: UUID,
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelDepositLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelDepositLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminCancelLossLimit(
        actor: UUID,
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelLossLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelLossLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun adminCancelBetLimit(
        actor: UUID,
        playerId: UUID,
        command: CancelLimit
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCancelWagerLimit(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelWagerLimit(actor, command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun showLimits(playerId: UUID): Response.Data<List<FinancialLimits>> {
        return Response.Data(
            queryExecutor.execute(
                FinancialLimitsQO(
                    playerId
                )
            )
        )
    }
}
