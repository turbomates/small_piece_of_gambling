@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.api

import com.google.inject.Inject
import com.turbomates.openapi.ktor.emptyPost
import com.turbomates.openapi.ktor.get
import com.turbomates.openapi.ktor.post
import dev.tmsoft.lib.ktor.GenericPipeline
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.ktor.RouteConfigurer
import io.betforge.infrastructure.ktor.Router
import io.betforge.infrastructure.ktor.auth.Auth
import io.betforge.infrastructure.ktor.auth.UserPrincipal
import io.betforge.infrastructure.ktor.auth.resolvePrincipal
import io.betforge.infrastructure.ktor.security.activities.AdminActivity
import io.betforge.infrastructure.ktor.security.activities.authorize
import io.betforge.player.api.controller.EntryRestrictionController
import io.betforge.player.application.restriction.command.CreateCoolingOffPeriod
import io.betforge.player.application.restriction.command.CreateSelfExclusionPeriod
import io.betforge.player.application.restriction.queryobject.EntryRestrictions
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RestrictionsRouter @Inject constructor(
    routeConfigurer: RouteConfigurer,
    genericPipeline: GenericPipeline
) : Router(routeConfigurer, genericPipeline) {

    init {
        routeConfigurer.routing {
            route("/api") {
                playerRestrictionRouting()
                adminRestrictionRouting()
            }
        }
    }

    private fun Route.playerRestrictionRouting() {
        authenticate(*Auth.user) {
            route("/self-exclusion") {
                get<Response.Data<EntryRestrictions>>("/cooling-off") {
                    controller<EntryRestrictionController>(this).showCoolingOffPeriod(
                        resolvePrincipal<UserPrincipal>().id
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CreateSelfExclusionPeriod>("") { command ->
                    controller<EntryRestrictionController>(this).selfExclusionPeriod(
                        resolvePrincipal<UserPrincipal>().id,
                        command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CreateCoolingOffPeriod>("/cooling-off") { command ->
                    controller<EntryRestrictionController>(this).coolingOffPeriod(
                        resolvePrincipal<UserPrincipal>().id,
                        command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>>("/cooling-off/cancel") {
                    controller<EntryRestrictionController>(this).cancelCoolingOffPeriod(
                        resolvePrincipal<UserPrincipal>().id
                    )
                }
            }
        }
    }

    private fun Route.adminRestrictionRouting() {
        authenticate(*Auth.admin) {
            route("/admin/exclusion") {
                authorize(setOf(AdminActivity.VIEW_PLAYERS, AdminActivity.MANAGE_PLAYERS)) {
                    get<Response.Listing<EntryRestrictions>>("") {
                        controller<EntryRestrictionController>(this).show()
                    }
                    post<Response.Either<Response.Ok, Response.Errors>, CreateSelfExclusionPeriod, IDPath>("/{id}") { command, params ->
                        controller<EntryRestrictionController>(this).selfExclusionPeriod(
                            params.id,
                            command
                        )
                    }
                    emptyPost<Response.Either<Response.Ok, Response.Errors>, IDPath>("/cancel/{id}") { params ->
                        controller<EntryRestrictionController>(this).cancelSelfExclusionPeriod(params)
                    }
                }
            }
        }
    }
}

@Serializable
data class IDPath(val id: UUID)
