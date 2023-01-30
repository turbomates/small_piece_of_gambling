package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.model.verification.VerificationTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import java.util.UUID

class PlayerVerificationsQO(private val player: UUID) : QueryObject<List<Verification>> {
    override suspend fun getData(): List<Verification> {
        val query = VerificationTable
            .join(UserTable, JoinType.LEFT, VerificationTable.player, UserTable.id)
            .join(
                UsernamePasswordCredentialsTable,
                JoinType.LEFT,
                VerificationTable.player,
                UsernamePasswordCredentialsTable.id
            )
            .select { VerificationTable.player eq player }
            .orderBy(VerificationTable.type, SortOrder.DESC)
        return query.map { it.toVerification() }
    }
}
