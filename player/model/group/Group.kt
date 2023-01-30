package io.betforge.player.model.group

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import dev.tmsoft.lib.exposed.type.array
import io.betforge.player.model.event.group.PlayerAddedToGroup
import io.betforge.player.model.event.group.PlayerRemovedFromGroup
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

class Group private constructor(id: EntityID<UUID>) : Entity<UUID>(id), AggregateRoot {
    private var name by PlayerGroupsTable.name
    private var filterId by PlayerGroupsTable.filterId
    private var playerIds by PlayerGroupsTable.playerIds
    private var color by PlayerGroupsTable.color
    private var priority by PlayerGroupsTable.priority
    private var updatedAt by PlayerGroupsTable.updatedAt

    companion object : PrivateEntityClass<UUID, Group>(object : Group.Repository() {}) {
        fun create(name: String, filterId: UUID?, playerIds: List<UUID>, color: String, priority: Int): Group {
            val group = Group.new {
                this.name = name
                this.filterId = filterId
                this.playerIds = playerIds.toTypedArray()
                this.color = color
                this.priority = priority
            }

            return group
        }
    }

    fun edit(name: String, color: String, priority: Int, filterId: UUID? = null) {
        this.name = name
        this.color = color
        this.priority = priority
        this.updatedAt = LocalDateTime.now()
        this.filterId = filterId
    }

    fun addPlayers(playerIds: Set<UUID>) {
        val uniqueNewPlayerIds = playerIds - this.playerIds.toSet()
        this.playerIds += uniqueNewPlayerIds.toTypedArray()

        uniqueNewPlayerIds.forEach {
            val event = PlayerAddedToGroup(id.value, it)
            addEvent(event)
        }
    }

    fun removePlayers(playerIds: Set<UUID>) {
        val currentPlayerIds = this.playerIds.toSet()
        val existedPlayerIds = currentPlayerIds.intersect(playerIds)
        val updatedPlayerIds = currentPlayerIds - existedPlayerIds
        this.playerIds = updatedPlayerIds.toTypedArray()

        existedPlayerIds.forEach {
            val event = PlayerRemovedFromGroup(id.value, it)
            addEvent(event)
        }
    }

    fun updatePlayers(playerIds: Set<UUID>) {
        val currentPlayerIds = this.playerIds.toSet()
        val userIdsForRemove = currentPlayerIds - playerIds
        val userIdsForAdd = playerIds - currentPlayerIds
        this.playerIds = playerIds.toTypedArray()

        userIdsForAdd.forEach {
            val event = PlayerAddedToGroup(id.value, it)
            addEvent(event)
        }

        userIdsForRemove.forEach {
            val event = PlayerRemovedFromGroup(id.value, it)
            addEvent(event)
        }
    }

    abstract class Repository : EntityClass<UUID, Group>(PlayerGroupsTable, Group::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): Group {
            return Group(entityId)
        }
    }
}

object PlayerGroupsTable : UUIDTable("player_groups") {
    val name = varchar("name", 255)
    val filterId = uuid("filter_id").nullable()
    val playerIds = array<UUID>("player_ids", UUIDColumnType())
    val color = varchar("color", 255)
    val priority = integer("priority")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
