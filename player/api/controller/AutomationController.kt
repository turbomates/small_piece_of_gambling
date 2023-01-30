package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.automation.command.CreateAutomation
import io.betforge.player.application.automation.command.Handler
import io.betforge.player.application.automation.command.Validation
import io.betforge.player.application.automation.queryobject.Automation
import io.betforge.player.application.automation.queryobject.AutomationsQO

class AutomationController @Inject constructor(
    private val handler: Handler,
    private val validation: Validation,
    private val queryExecutor: QueryExecutor,
) : Controller() {
    suspend fun create(command: CreateAutomation): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.validate(command)
        if (errors.isNotEmpty()) return Response.Either(Either.Right(Response.Errors(errors)))

        handler.handle(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun automations(): Response.Listing<Automation> {
        return Response.Listing(queryExecutor.execute(AutomationsQO(call.request.pagingParameters())))
    }
}
