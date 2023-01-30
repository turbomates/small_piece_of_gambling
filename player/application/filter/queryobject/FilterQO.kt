@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.filter.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.exposed.query.fold
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousListBuilder
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.application.filter.condition.Condition
import io.betforge.player.model.PlayerFilterTable
import io.betforge.player.model.group.PlayerGroupsTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class AllFiltersQO : QueryObject<List<Filter>> {
    override suspend fun getData(): List<Filter> {
        return PlayerFilterTable.selectAll().map { it.toFilter() }
    }
}

class SingleFilterQO(private val filterId: UUID) : QueryObject<Filter> {
    override suspend fun getData(): Filter {
        return PlayerFilterTable.select { PlayerFilterTable.id eq filterId }.single().toFilter()
    }
}

class FilterQO(private val paging: PagingParameters) : QueryObject<ContinuousList<Filter>> {
    override suspend fun getData(): ContinuousList<Filter> {
        return PlayerFilterTable
            .join(PlayerGroupsTable, JoinType.LEFT, PlayerGroupsTable.filterId, PlayerFilterTable.id)
            .selectAll()
            .orderBy(PlayerFilterTable.name, SortOrder.ASC)
            .toContinuousListBuilder(paging) {
                fold(PlayerFilterTable, { toFilter() }, mapOf(
                    PlayerGroupsTable to { filter ->
                        val groupId = this[PlayerGroupsTable.id].value
                        filter.apply { groupIds.add(groupId) }
                    }
                ))
            }
    }
}

fun ResultRow.toFilter() = Filter(
    this[PlayerFilterTable.id].value,
    this[PlayerFilterTable.name],
    this[PlayerFilterTable.conditions],
    this[PlayerFilterTable.count]
)

@Serializable
data class Filter(
    val id: UUID,
    val name: String,
    val conditions: List<Condition>,
    val playerCount: Int,
    val groupIds: MutableList<UUID> = mutableListOf()
)
