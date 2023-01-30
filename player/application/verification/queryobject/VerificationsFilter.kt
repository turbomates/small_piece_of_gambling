package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.query.filter.Filter
import dev.tmsoft.lib.query.filter.addJoin
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.application.verification.type.BlockedList
import io.betforge.player.application.verification.type.Email
import io.betforge.player.application.verification.type.GameStop
import io.betforge.player.application.verification.type.IDScan
import io.betforge.player.model.verification.VerificationTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.OrOp
import org.jetbrains.exposed.sql.andWhere

object VerificationsFilter : Filter(VerificationTable) {
    val type = add("type", VerificationTable.type, listOf(BlockedList.name, Email.name, GameStop.name, IDScan.name))
    val status = add("status", VerificationTable.status)
    val changedAt = add("changed_at")
    val player = add("player", VerificationTable.player) { values ->
        addJoin {
            join(
                UsernamePasswordCredentialsTable,
                JoinType.LEFT,
                VerificationTable.player,
                UsernamePasswordCredentialsTable.id
            )
        }.addJoin {
            join(
                UserTable,
                JoinType.LEFT,
                VerificationTable.player,
                UserTable.id
            )
        }.andWhere {
            OrOp(
                values.map {
                    OrOp(
                        listOf(it.op(UsernamePasswordCredentialsTable.email.address), it.op(UserTable.username))
                    )
                }
            )
        }
    }
}
