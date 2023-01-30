package io.betforge.player.application.restriction.queryobject

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousList
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.model.restrictions.EntryRestrictionsTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.UUID

class EntryRestrictionsQO(
    private val paging: PagingParameters
) : QueryObject<ContinuousList<EntryRestrictions>> {
    override suspend fun getData(): ContinuousList<EntryRestrictions> {
        return EntryRestrictionsTable
            .select {
                EntryRestrictionsTable.endedAt.greater(LocalDateTime.now())
            }
            .toContinuousList(paging, ResultRow::toEntryRestrictions)
    }
}

fun ResultRow.toEntryRestrictions() = EntryRestrictions(
    this[EntryRestrictionsTable.id].value,
    this[EntryRestrictionsTable.playerId],
    this[EntryRestrictionsTable.startedAt],
    this[EntryRestrictionsTable.endedAt]
)

@Serializable
data class EntryRestrictions(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val playerId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startedAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endedAt: LocalDateTime
)
