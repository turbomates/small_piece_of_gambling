package io.betforge.player.api.controller

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.ktor.Controller
import dev.tmsoft.lib.ktor.Response
import dev.tmsoft.lib.query.filter.filterValues
import dev.tmsoft.lib.query.paging.pagingParameters
import dev.tmsoft.lib.upload.FileManagerFactory
import io.betforge.player.application.Player
import io.betforge.player.application.PlayerQO
import io.betforge.player.application.PlayersQO
import java.util.UUID

class PlayerController @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val fileManager: FileManagerFactory
) : Controller() {

    suspend fun show(playerId: UUID): Response.Data<Player> {
        return Response.Data(queryExecutor.execute(PlayerQO(playerId, fileManager.current())))
    }

    suspend fun players(): Response.Listing<Player> {
        return Response.Listing(
            queryExecutor.execute(
                PlayersQO(
                    call.request.pagingParameters(),
                    fileManager.current(),
                    call.parameters.filterValues()
                )
            )
        )
    }
}
