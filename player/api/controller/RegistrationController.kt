package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.structure.Either
import io.betforge.player.application.registration.European
import io.betforge.player.application.registration.Handler
import io.betforge.player.application.registration.Simple
import io.betforge.player.application.registration.Telegram
import io.betforge.player.application.registration.UK
import io.betforge.player.application.registration.Validation

class RegistrationController @Inject constructor(
    private val handler: Handler,
    private val validation: Validation
) : Controller() {

    suspend fun register(command: European): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onRegisterEuropean(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleRegister(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun registerSimple(command: Simple): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onRegisterSimple(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleRegisterSimple(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun registerUK(command: UK): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onRegisterUK(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleRegisterUK(command)
        return Response.Either(Either.Left(Response.Ok))
    }

    suspend fun registerTelegram(command: Telegram): Response.Either<Response.Ok, Response.Errors> {
        val errors = validation.onRegisterTelegram(command)
        if (errors.isNotEmpty()) {
            return Response.Either(Either.Right(Response.Errors(errors)))
        }
        handler.handleRegisterTelegram(command)
        return Response.Either(Either.Left(Response.Ok))
    }
}
