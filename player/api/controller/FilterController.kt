package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.filter.Handler
import io.betforge.player.application.filter.Validation
import io.betforge.player.application.filter.command.AddFilter
import io.betforge.player.application.filter.command.DeleteFilter
import io.betforge.player.application.filter.command.EditFilter
import io.betforge.player.application.filter.queryobject.Filter
import io.betforge.player.application.filter.queryobject.FilterQO

class FilterController @Inject constructor(
    private val handler: Handler,
    private val queryExecutor: QueryExecutor,
    private val validation: Validation
) : Controller() {
    suspend fun add(command: AddFilter): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onAddFilter(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.addFilter(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun edit(command: EditFilter): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onEditFilter(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.editFilter(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun delete(command: DeleteFilter): Response.Either<Response.Ok, Response.Errors> {
        handler.deleteFilter(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun filters(): Response.Listing<Filter> {
        return Response.Listing(
            queryExecutor.execute(
                FilterQO(
                    call.request.pagingParameters()
                )
            )
        )
    }
}
