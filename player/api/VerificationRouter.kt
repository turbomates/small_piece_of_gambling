@file:UseSerializers(FileSerializer::class)

package io.betforge.player.api

import com.google.inject.Inject
import com.turbomates.openapi.ktor.get
import com.turbomates.openapi.ktor.post
import dev.tmsoft.lib.ktor.GenericPipeline
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.filter.filterDescription
import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.upload.FileSerializer
import io.betforge.infrastructure.ktor.RouteConfigurer
import io.betforge.infrastructure.ktor.Router
import io.betforge.infrastructure.ktor.auth.Auth
import io.betforge.infrastructure.ktor.auth.UserPrincipal
import io.betforge.infrastructure.ktor.auth.resolvePrincipal
import io.betforge.player.api.controller.VerificationController
import io.betforge.player.application.verification.queryobject.Verification
import io.betforge.player.application.verification.queryobject.VerificationsFilter
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

class VerificationRouter @Inject constructor(
    routeConfigurer: RouteConfigurer,
    genericPipeline: GenericPipeline
) : Router(routeConfigurer, genericPipeline) {

    init {
        routeConfigurer.routing {
            route("/api") {
                playerVerificationRouting()
                adminVerificationRouting()
            }
        }
    }

    private fun Route.playerVerificationRouting() {
        route("/player") {
            authenticate(*Auth.user) {
                post<Response.Either<Response.Ok, Response.Errors>, IDScan>("/verification/id-scan") { command ->
                    controller<VerificationController>(this).idScanVerification(
                        resolvePrincipal<UserPrincipal>().id,
                        command.idScan
                    )
                }
                get<Response.Data<List<Verification>>>("/verifications") {
                    controller<VerificationController>(this).playerVerifications(resolvePrincipal<UserPrincipal>().id)
                }
            }
            get<Response.Either<Response.Ok, Response.Error>, PathToken>("/verification/email") { command ->
                controller<VerificationController>(this).emailVerification(
                    command.token
                )
            }
        }
        route("/verifications") {
            get<Response.Data<Set<String>>>("/all") {
                controller<VerificationController>(this).all()
            }
        }
    }

    private fun Route.adminVerificationRouting() {
        authenticate(*Auth.admin) {
            route("/admin/verifications") {
                get<Response.Listing<Verification>>("") {
                    controller<VerificationController>(this).list()
                }.filterDescription(VerificationsFilter)
                get<Response.Data<Verification>, PathId>("/{id}") { path ->
                    controller<VerificationController>(this).show(path.id)
                }
                get<Response.Data<Long>>("/total") {
                    controller<VerificationController>(this).total()
                }.filterDescription(VerificationsFilter)
                post<Response.Either<Response.Ok, Response.Error>, Reason, VerificationChanges>("/{player}/{key}/decline") { body, path ->
                    controller<VerificationController>(this).decline(path.player, path.key, body.reason)
                }
                post<Response.Either<Response.Ok, Response.Error>, Reason, VerificationChanges>("/{player}/{key}/approve") { body, path ->
                    controller<VerificationController>(this).approve(path.player, path.key, body.reason)
                }
            }
        }
    }

    data class PathId(val id: UUID)
    data class PathToken(val token: UUID)
    data class VerificationChanges(val player: UUID, val key: String)

    @Serializable
    data class IDScan(val idScan: List<File>)

    @Serializable
    data class Reason(val reason: String)
}
