package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.model.verification.VerificationTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.UUID

class VerificationQO(private val id: UUID) : QueryObject<Verification> {
    override suspend fun getData(): Verification {
        return VerificationTable
            .join(UserTable, JoinType.LEFT, VerificationTable.player, UserTable.id)
            .join(
                UsernamePasswordCredentialsTable,
                JoinType.LEFT,
                VerificationTable.player,
                UsernamePasswordCredentialsTable.id
            )
            .select { VerificationTable.id eq id }
            .single()
            .toVerification()
    }
}
