package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.query.filter.PathValues
import dev.tmsoft.lib.query.filter.filter
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousList
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.model.verification.VerificationTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class VerificationsQO(private val paging: PagingParameters, private val filterValues: PathValues = PathValues()) :
    QueryObject<ContinuousList<Verification>> {
    override suspend fun getData(): ContinuousList<Verification> {
        val query = VerificationTable
            .join(UserTable, JoinType.LEFT, VerificationTable.player, UserTable.id)
            .join(
                UsernamePasswordCredentialsTable,
                JoinType.LEFT,
                VerificationTable.player,
                UsernamePasswordCredentialsTable.id
            )
            .selectAll()
            .filter(VerificationsFilter, filterValues)
            .orderBy(VerificationTable.type, SortOrder.DESC)
        return query.toContinuousList(paging, ResultRow::toVerification)
    }
}
