@file:UseSerializers(FileSerializer::class)

package io.betforge.player.api

import com.google.inject.Inject
import com.turbomates.openapi.ktor.get
import com.turbomates.openapi.ktor.post
import dev.tmsoft.lib.ktor.GenericPipeline
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.filter.filterDescription
import dev.tmsoft.lib.upload.FileSerializer
import io.betforge.infrastructure.ktor.RouteConfigurer
import io.betforge.infrastructure.ktor.Router
import io.betforge.infrastructure.ktor.auth.Auth
import io.betforge.infrastructure.ktor.auth.UserPrincipal
import io.betforge.infrastructure.ktor.auth.resolvePrincipal
import io.betforge.infrastructure.ktor.security.activities.AdminActivity
import io.betforge.infrastructure.ktor.security.activities.authorize
import io.betforge.player.api.controller.AutomationController
import io.betforge.player.api.controller.FilterController
import io.betforge.player.api.controller.GroupController
import io.betforge.player.api.controller.PlayerController
import io.betforge.player.api.controller.RegistrationController
import io.betforge.player.api.controller.SettingsController
import io.betforge.player.application.Player
import io.betforge.player.application.PlayersFilter
import io.betforge.player.application.automation.command.CreateAutomation
import io.betforge.player.application.automation.queryobject.Automation
import io.betforge.player.application.filter.command.AddFilter
import io.betforge.player.application.filter.command.DeleteFilter
import io.betforge.player.application.filter.command.EditFilter
import io.betforge.player.application.filter.queryobject.Filter
import io.betforge.player.application.group.command.AddPlayersToGroup
import io.betforge.player.application.group.command.CreateGroup
import io.betforge.player.application.group.command.DeleteGroup
import io.betforge.player.application.group.command.EditGroup
import io.betforge.player.application.group.command.RemovePlayersFromGroup
import io.betforge.player.application.group.queryobject.Group
import io.betforge.player.application.registration.European
import io.betforge.player.application.registration.Simple
import io.betforge.player.application.registration.Telegram
import io.betforge.player.application.registration.UK
import io.betforge.player.application.settings.command.EditDetails
import io.betforge.player.application.settings.command.UpdateAvatar
import io.betforge.player.application.settings.queryobject.Details
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.UseSerializers
import java.util.UUID

class Router @Inject constructor(
    routeConfigurer: RouteConfigurer,
    genericPipeline: GenericPipeline
) : Router(routeConfigurer, genericPipeline) {

    init {
        routeConfigurer.routing {
            route("/api") {
                registrationRouting()
                playerRouting()
                adminRouting()
            }
        }
    }

    private fun Route.registrationRouting() {
        route("/player/register") {
            post<Response.Either<Response.Ok, Response.Errors>, European>("") { command ->
                controller<RegistrationController>(this).register(command)
            }
            post<Response.Either<Response.Ok, Response.Errors>, Simple>("/simple") { command ->
                controller<RegistrationController>(this).registerSimple(command)
            }
            post<Response.Either<Response.Ok, Response.Errors>, UK>("/uk") { command ->
                controller<RegistrationController>(this).registerUK(command)
            }
            post<Response.Either<Response.Ok, Response.Errors>, Telegram>("/telegram") { command ->
                controller<RegistrationController>(this).registerTelegram(command)
            }
        }
    }

    private fun Route.adminRouting() {
        authenticate(*Auth.admin) {
            route("/admin/players") {
                authorize(setOf(AdminActivity.VIEW_PLAYERS, AdminActivity.MANAGE_PLAYERS)) {
                    get<Response.Listing<Player>>("") {
                        controller<PlayerController>(this).players()
                    }.filterDescription(PlayersFilter)

                    get<Response.Data<Player>, PathId>("/{id}") { params ->
                        controller<PlayerController>(this).show(params.id)
                    }

                    get<Response.Data<Details>, PathId>("/details/{id}") { params ->
                        controller<SettingsController>(this).showDetails(params.id)
                    }

                    get<Response.Listing<Filter>>("/filters") {
                        controller<FilterController>(this).filters()
                    }
                }
                authorize(setOf(AdminActivity.MANAGE_PLAYERS)) {
                    post<Response.Either<Response.Ok, Response.Errors>, EditDetails, PathId>("/details/{id}") { command, params ->
                        controller<SettingsController>(this).editDetails(params.id, command)
                    }
                }
                authorize(setOf(AdminActivity.MANAGE_FILTERS)) {
                    post<Response.Either<Response.Ok, Response.Errors>, AddFilter>("/filters/add") { command ->
                        controller<FilterController>(this).add(command)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, EditFilter>("/filters/edit") { command ->
                        controller<FilterController>(this).edit(command)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, DeleteFilter>("/filters/delete") { command ->
                        controller<FilterController>(this).delete(command)
                    }
                }
                authorize(setOf(AdminActivity.MANAGE_PLAYERS)) {
                    post<Response.Either<Response.Ok, Response.Errors>, CreateGroup>("/groups/create") { command ->
                        controller<GroupController>(this).create(command)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, EditGroup>("/groups/edit") { command ->
                        controller<GroupController>(this).edit(command)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, DeleteGroup>("/groups/delete") { command ->
                        controller<GroupController>(this).delete(command)
                    }

                    get<Response.Listing<Group>>("/groups") {
                        controller<GroupController>(this).groups()
                    }

                    get<Response.Data<Group>, PathId>("/groups/{id}") { params ->
                        controller<GroupController>(this).group(params.id)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, AddPlayersToGroup>("/groups/add_players") { command ->
                        controller<GroupController>(this).addPlayers(command)
                    }

                    post<Response.Either<Response.Ok, Response.Errors>, RemovePlayersFromGroup>("/groups/remove_players") { command ->
                        controller<GroupController>(this).removePlayers(command)
                    }
                }
                route("/automations") {
                    authorize(setOf(AdminActivity.MANAGE_AUTOMATIONS)) {
                        get<Response.Listing<Automation>>("") {
                            controller<AutomationController>(this).automations()
                        }
                        post<Response.Either<Response.Ok, Response.Errors>, CreateAutomation>("/create") { command ->
                            controller<AutomationController>(this).create(command)
                        }
                    }
                }
            }
        }
    }

    private fun Route.playerRouting() {
        authenticate(*Auth.user) {
            route("/players/me") {
                get<Response.Data<Player>>("") { controller<PlayerController>(this).show(resolvePrincipal<UserPrincipal>().id) }

                get<Response.Data<Details>>("/details") {
                    controller<SettingsController>(this).showDetails(
                        resolvePrincipal<UserPrincipal>().id
                    )
                }

                post<Response.Either<Response.Ok, Response.Errors>, EditDetails>("/details") { command ->
                    controller<SettingsController>(this).editDetails(resolvePrincipal<UserPrincipal>().id, command)
                }

                post<Response.Either<Response.Ok, Response.Errors>, UpdateAvatar>("/avatar") { command ->
                    controller<SettingsController>(this).updateAvatar(resolvePrincipal<UserPrincipal>().id, command)
                }
            }
        }
    }

    data class PathId(val id: UUID)
}
