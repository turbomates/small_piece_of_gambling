@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.group.queryobject

import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.exposed.type.any
import dev.tmsoft.lib.query.paging.ContinuousList
import dev.tmsoft.lib.query.paging.PagingParameters
import dev.tmsoft.lib.query.paging.toContinuousList
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.player.model.group.PlayerGroupsTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class GroupIdsByUserId(private val userId: UUID) : QueryObject<List<UUID>> {
    override suspend fun getData(): List<UUID> {
        return PlayerGroupsTable
            .slice(PlayerGroupsTable.id)
            .select { PlayerGroupsTable.playerIds any userId }
            .map { it[PlayerGroupsTable.id].value }
    }
}

class GroupsByFilterQO(private val filterId: UUID) : QueryObject<List<Group>> {
    override suspend fun getData(): List<Group> {
        return PlayerGroupsTable
            .select { PlayerGroupsTable.filterId eq filterId }
            .map { it.toGroup() }
    }
}

class GroupsQO(private val paging: PagingParameters) : QueryObject<ContinuousList<Group>> {
    override suspend fun getData(): ContinuousList<Group> {
        return PlayerGroupsTable
            .selectAll()
            .orderBy(PlayerGroupsTable.createdAt, SortOrder.DESC)
            .toContinuousList(paging, ResultRow::toGroup)
    }
}

class GroupQO(private val groupId: UUID) : QueryObject<Group> {
    override suspend fun getData(): Group {
        return PlayerGroupsTable
            .select { PlayerGroupsTable.id eq groupId }
            .single()
            .toGroup()
    }
}

class GroupPlayerIdsQO(private val groupId: UUID) : QueryObject<List<UUID>> {
    override suspend fun getData(): List<UUID> {
        return PlayerGroupsTable
            .slice(PlayerGroupsTable.playerIds)
            .select { PlayerGroupsTable.id eq groupId }
            .single()[PlayerGroupsTable.playerIds]
            .toList()
    }
}

fun ResultRow.toGroup() = Group(
    this[PlayerGroupsTable.id].value,
    this[PlayerGroupsTable.name],
    this[PlayerGroupsTable.filterId],
    this[PlayerGroupsTable.playerIds].size,
    this[PlayerGroupsTable.color],
    this[PlayerGroupsTable.priority]
)

@Serializable
data class Group(
    val id: UUID,
    val name: String,
    val filterId: UUID?,
    val playersCount: Int,
    val color: String,
    val priority: Int
)
