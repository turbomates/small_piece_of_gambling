package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.structure.Either
import io.betforge.player.api.IDPath
import io.betforge.player.application.restriction.command.CancelCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateSelfExclusionPeriod
import io.betforge.player.application.restriction.Handler
import io.betforge.player.application.restriction.Validation
import io.betforge.player.application.restriction.queryobject.CoolingOffPeriodQO
import io.betforge.player.application.restriction.queryobject.EntryRestrictions
import io.betforge.player.application.restriction.queryobject.EntryRestrictionsQO
import java.util.UUID

class EntryRestrictionController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val handler: Handler,
    private val validation: Validation
) : Controller() {
    suspend fun coolingOffPeriod(
        playerId: UUID,
        command: CreateCoolingOffPeriod
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onCoolingOffPeriod(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.createCoolingOffPeriod(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun cancelCoolingOffPeriod(
        playerId: UUID
    ): Response.Either<Response.Ok, Response.Errors> {
        val command = CancelCoolingOffPeriod(playerId)
        val errors = validation.onCancelCoolingOffPeriod(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelCoolingOffPeriod(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun selfExclusionPeriod(
        playerId: UUID,
        command: CreateSelfExclusionPeriod
    ): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onSelfExclusionPeriod(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.createSelfExclusionPeriod(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun cancelSelfExclusionPeriod(
        params: IDPath
    ): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onCancelSelfExclusionPeriod(params)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.cancelSelfExclusionPeriod(params)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun show(): Response.Listing<EntryRestrictions> {
        return Response.Listing(
            queryExecutor.execute(
                EntryRestrictionsQO(
                    call.request.pagingParameters()
                )
            )
        )
    }

    suspend fun showCoolingOffPeriod(playerId: UUID): Response.Data<EntryRestrictions> {
        return Response.Data(queryExecutor.execute(CoolingOffPeriodQO(playerId)))
    }
}
