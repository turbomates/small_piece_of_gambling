package io.betforge.player.application.verification

import com.google.inject.Inject
import dev.tmsoft.lib.exposed.query.QueryExecutor
import dev.tmsoft.lib.upload.FileManagerFactory
import io.betforge.player.application.PlayerQO
import io.betforge.player.model.verification.Address
import io.betforge.player.model.verification.Bio
import io.betforge.player.model.verification.VerificationInfo
import io.betforge.player.model.verification.VerificationInfoLoader
import java.util.UUID

class VerificationInfoLoader @Inject constructor(
    private val queryExecutor: QueryExecutor,
    private val fileManager: FileManagerFactory
) : VerificationInfoLoader {
    override suspend fun load(playerId: UUID): VerificationInfo {
        val player = queryExecutor.execute(PlayerQO(playerId, fileManager.current()))

        return VerificationInfo(
            playerId,
            Bio(player.firstName.orEmpty(), player.lastName.orEmpty(), player.birthday?.toString().orEmpty()),
            Address(player.firstName.orEmpty()),
            player.email.orEmpty()
        )
    }
}
