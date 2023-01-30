package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.group.command.AddPlayersToGroup
import io.betforge.player.application.group.command.CreateGroup
import io.betforge.player.application.group.command.DeleteGroup
import io.betforge.player.application.group.command.EditGroup
import io.betforge.player.application.group.command.Handler
import io.betforge.player.application.group.command.RemovePlayersFromGroup
import io.betforge.player.application.group.command.Validation
import io.betforge.player.application.group.queryobject.Group
import io.betforge.player.application.group.queryobject.GroupQO
import io.betforge.player.application.group.queryobject.GroupsQO
import java.util.UUID

class GroupController @Inject constructor(
    private val handler: Handler,
    private val validation: Validation,
    private val queryExecutor: QueryExecutor,
) : Controller() {
    suspend fun create(command: CreateGroup): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onCreateGroup(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun edit(command: EditGroup): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditGroup(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun delete(command: DeleteGroup): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onDeleteGroup(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun addPlayers(command: AddPlayersToGroup): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddPlayersToGroup(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun removePlayers(command: RemovePlayersFromGroup): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onRemovePlayersFromGroup(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun groups(): Response.Listing<Group> {
        return Response.Listing(
            queryExecutor.execute(
                GroupsQO(
                    call.request.pagingParameters()
                )
            )
        )
    }

    suspend fun group(groupId: UUID): Response.Data<Group> {
        return Response.Data(queryExecutor.execute(GroupQO(groupId)))
    }
}
