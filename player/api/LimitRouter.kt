package io.betforge.player.api

import com.google.inject.Inject
import com.turbomates.openapi.ktor.emptyPost
import com.turbomates.openapi.ktor.get
import com.turbomates.openapi.ktor.post
import dev.tmsoft.lib.ktor.GenericPipeline
import dev.tmsoft.lib.ktor.Response
import io.betforge.infrastructure.ktor.RouteConfigurer
import io.betforge.infrastructure.ktor.Router
import io.betforge.infrastructure.ktor.auth.AdminPrincipal
import io.betforge.infrastructure.ktor.auth.Auth
import io.betforge.infrastructure.ktor.auth.UserPrincipal
import io.betforge.infrastructure.ktor.auth.resolvePrincipal
import io.betforge.player.api.controller.AdminLimitsController
import io.betforge.player.api.controller.PlayerLimitsController
import io.betforge.player.application.limits.admin.command.AddDepositLimit
import io.betforge.player.application.limits.admin.command.AddLossLimit
import io.betforge.player.application.limits.admin.command.AddMaxBetLimit
import io.betforge.player.application.limits.admin.command.AddWagerLimit
import io.betforge.player.application.limits.admin.command.AddWithdrawLimit
import io.betforge.player.application.limits.admin.command.EditDepositLimit
import io.betforge.player.application.limits.admin.command.EditLossLimit
import io.betforge.player.application.limits.admin.command.EditMaxBetLimit
import io.betforge.player.application.limits.admin.command.EditWagerLimit
import io.betforge.player.application.limits.admin.command.EditWithdrawLimit
import io.betforge.player.application.limits.admin.query.FinancialLimit
import io.betforge.player.application.limits.player.command.AddLimit
import io.betforge.player.application.limits.player.command.CancelLimit
import io.betforge.player.application.limits.player.command.EditLimit
import io.betforge.player.application.limits.player.queryobject.FinancialLimits
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import java.util.UUID

class LimitRouter @Inject constructor(
    routeConfigurer: RouteConfigurer,
    genericPipeline: GenericPipeline
) : Router(routeConfigurer, genericPipeline) {

    init {
        routeConfigurer.routing {
            route("/api") {
                playerLimitRouting()
                adminLimitRouting()
            }
        }
    }

    private fun Route.playerLimitRouting() {
        authenticate(*Auth.user) {
            route("/self-exclusion") {
                get<Response.Data<List<FinancialLimits>>>("/limits") {
                    controller<PlayerLimitsController>(this).showLimits(
                        resolvePrincipal<UserPrincipal>().id
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddLimit>("/deposit/add") { command ->
                    controller<PlayerLimitsController>(this).addDepositLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit>("/deposit/edit") { command ->
                    controller<PlayerLimitsController>(this).editDepositLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit>("/deposit/cancel") { command ->
                    controller<PlayerLimitsController>(this).cancelDepositLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddLimit>("/loss/add") { command ->
                    controller<PlayerLimitsController>(this).addLossLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit>("/loss/edit") { command ->
                    controller<PlayerLimitsController>(this).editLossLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit>("/loss/cancel") { command ->
                    controller<PlayerLimitsController>(this).cancelLossLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddLimit>("/wager/add") { command ->
                    controller<PlayerLimitsController>(this).addWagerLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit>("/wager/edit") { command ->
                    controller<PlayerLimitsController>(this).editWagerLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit>("/wager/cancel") { command ->
                    controller<PlayerLimitsController>(this).cancelWagerLimit(
                        resolvePrincipal<UserPrincipal>().id, command
                    )
                }
            }
        }
        authenticate(*Auth.admin) {
            route("/admin/exclusion/player-limits") {
                get<Response.Data<List<FinancialLimits>>, IDPath>("/{id}") { params ->
                    controller<PlayerLimitsController>(this).showLimits(params.id)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit, IDPath>("/deposit/edit/{id}") { command, params ->
                    command.playerId = params.id
                    controller<PlayerLimitsController>(this).adminEditDepositLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit, IDPath>("/loss/edit/{id}") { command, params ->
                    command.playerId = params.id
                    controller<PlayerLimitsController>(this).adminEditLossLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLimit, IDPath>("/bet/edit/{id}") { command, params ->
                    controller<PlayerLimitsController>(this).adminEditWagerLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit, IDPath>("/deposit/cancel/{id}") { command, params ->
                    controller<PlayerLimitsController>(this).adminCancelDepositLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit, IDPath>("/loss/cancel/{id}") { command, params ->
                    controller<PlayerLimitsController>(this).adminCancelLossLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
                post<Response.Either<Response.Ok, Response.Errors>, CancelLimit, IDPath>("/bet/cancel/{id}") { command, params ->
                    controller<PlayerLimitsController>(this).adminCancelBetLimit(
                        resolvePrincipal<AdminPrincipal>().id, params.id, command
                    )
                }
            }
        }
    }

    private fun Route.adminLimitRouting() {
        route("/admin/exclusion/admin-limits") {
            authenticate(*Auth.admin) {
                get<Response.Data<List<FinancialLimit>>, IDPath>("/player/{id}") { params ->
                    controller<AdminLimitsController>(this).playerLimits(params.id)
                }
                get<Response.Data<List<FinancialLimit>>, IDPath>("/group/{id}") { params ->
                    controller<AdminLimitsController>(this).groupLimits(params.id)
                }
                get<Response.Data<List<FinancialLimit>>>("/application") {
                    controller<AdminLimitsController>(this).applicationLimits()
                }
                get<Response.Data<FinancialLimit>, IDPath>("/{id}") { params ->
                    controller<AdminLimitsController>(this).show(params.id)
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddWagerLimit>("/wager/add") { command ->
                    controller<AdminLimitsController>(this).addWagerLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddLossLimit>("/loss/add") { command ->
                    controller<AdminLimitsController>(this).addLossLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddDepositLimit>("/deposit/add") { command ->
                    controller<AdminLimitsController>(this).addDepositLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddMaxBetLimit>("/max-bet/add") { command ->
                    controller<AdminLimitsController>(this).addMaxBetLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, AddWithdrawLimit>("/withdraw/add") { command ->
                    controller<AdminLimitsController>(this).addWithdrawLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditWagerLimit, IDPath>("/wager/edit/{id}") { command, params ->
                    command.limitId = params.id
                    controller<AdminLimitsController>(this).editWagerLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditLossLimit, IDPath>("/loss/edit/{id}") { command, params ->
                    command.limitId = params.id
                    controller<AdminLimitsController>(this).editLossLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditDepositLimit, IDPath>("/deposit/edit/{id}") { command, params ->
                    command.limitId = params.id
                    controller<AdminLimitsController>(this).editDepositLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditWithdrawLimit, IDPath>("/withdraw/edit/{id}") { command, params ->
                    command.limitId = params.id
                    controller<AdminLimitsController>(this).editWithdrawLimit(command)
                }
                post<Response.Either<Response.Ok, Response.Errors>, EditMaxBetLimit, IDPath>("/max-bet/edit/{id}") { command, params ->
                    command.limitId = params.id
                    controller<AdminLimitsController>(this).editMaxBetLimit(command)
                }
                emptyPost<Response.Either<Response.Ok, Response.Errors>, IDPath>("/delete/{id}") { params ->
                    controller<AdminLimitsController>(this).deleteLimit(params.id)
                }
            }
        }
    }

    data class IDPath(val id: UUID)
}
