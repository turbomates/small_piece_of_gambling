package io.betforge.player.model

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.upload.FileManager
import io.betforge.player.model.event.PlayerRegistered
import io.betforge.player.model.event.PlayerVerificationStatusApplied
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

typealias PlayerId = UUID
class Player private constructor(id: EntityID<PlayerId>) : Entity<PlayerId>(id), AggregateRoot {
    private var userId by PlayerTable.id
    private var status by PlayerTable.status
    private var details by PlayerTable.details
    private var avatar by PlayerTable.avatar

    companion object : PrivateEntityClass<PlayerId, Player>(object : Repository() {}) {
        fun register(
            id: PlayerId,
            details: Details? = null,
            status: Status? = null
        ): Player {
            val player = Player.new {
                this.userId = EntityID(id, PlayerTable)
                this.status = status ?: Status.FULL_ACCESS
                if (details != null) {
                    this.details = details
                }
            }
            player.addEvent(PlayerRegistered(id))
            return player
        }
    }

    abstract class Repository : EntityClass<PlayerId, Player>(PlayerTable, Player::class.java) {
        override fun createInstance(entityId: EntityID<PlayerId>, row: ResultRow?): Player {
            return Player(entityId)
        }
    }

    fun limit() {
        status = Status.LIMITED
    }

    fun suspend() {
        status = Status.SUSPENDED
    }

    fun ban() {
        status = Status.CLOSED
    }

    fun updateDetails(details: Details) {
        this.details = details
    }

    fun applyVerification(status: Status) {
        if (status != this.status) {
            this.status = status
            addEvent(PlayerVerificationStatusApplied(userId.value, status))
        }
    }

    suspend fun uploadAvatar(avatar: File, fileManager: FileManager) {
        this.avatar = fileManager.add(
            avatar,
            "avatars"
        )
    }
}

object PlayerTable : IdTable<PlayerId>("players") {
    override val id: Column<EntityID<PlayerId>> = uuid("user_id").entityId()
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
    val status = enumerationByName("status", 25, Status::class).default(Status.FULL_ACCESS)
    val details = details()
    val avatar = varchar("avatar", 255).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

enum class Status {
    FULL_ACCESS, LIMITED, CLOSED, SUSPENDED
}
