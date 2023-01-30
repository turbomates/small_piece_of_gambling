package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.settings.command.EditDetails
import io.betforge.player.application.settings.Handler
import io.betforge.player.application.settings.command.UpdateAvatar
import io.betforge.player.application.settings.Validation
import io.betforge.player.application.settings.queryobject.Details
import io.betforge.player.application.settings.queryobject.DetailsQO
import java.util.UUID

class SettingsController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val handler: Handler,
    private val validation: Validation

) : Controller() {

    suspend fun showDetails(playerId: UUID): Response.Data<Details> {
        return Response.Data(queryExecutor.execute(DetailsQO(playerId)))
    }

    suspend fun editDetails(playerId: UUID, command: EditDetails): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onEditDetails(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleEditDetails(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun updateAvatar(playerId: UUID, command: UpdateAvatar): Response.Either<Response.Ok, Response.Errors> {
        command.playerId = playerId
        val errors = validation.onUpdateAvatar(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleUpdateAvatar(command)
        return Response.Either(Either.Left(Response.Ok))
    }
}
