package io.betforge.player.application.verification.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.query.filter.PathValues
import dev.tmsoft.lib.query.filter.filter
import io.betforge.player.model.verification.VerificationTable
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll

class VerificationsCountQO(private val filterValues: PathValues = PathValues()) : QueryObject<Long> {
    override suspend fun getData(): Long {
        val query = VerificationTable.slice(VerificationTable.id.count()).selectAll()
            .filter(VerificationsFilter, filterValues)

        return query.single().run { this[VerificationTable.id.count()] }
    }
}
