package io.betforge.player.model.verification

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import dev.tmsoft.lib.exposed.type.jsonb
import io.betforge.player.infrasturcture.exceptions.IncorrectVerificationStatusException
import io.betforge.player.model.event.VerificationApproved
import io.betforge.player.model.event.VerificationDeclined
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

class Verification private constructor(
    id: EntityID<UUID>,
) : Entity<UUID>(id), AggregateRoot {

    private var _player by VerificationTable.player
    var type by VerificationTable.type
        private set
    private var status by VerificationTable.status
    private var reason by VerificationTable.reason
    private var _data by VerificationTable.data
    private var changedAt by VerificationTable.changedAt
    val data: List<JsonElement>
        get() = _data

    val player: UUID
        get() = _player

    companion object : PrivateEntityClass<UUID, Verification>(object : Repository() {}) {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun create(player: UUID, type: Verifier.Key<out Verifier>): Verification {
            return Verification.new {
                this.status = Status.WAITING
                this._player = player
                this.type = type.toString()
            }
        }
    }

    abstract class Repository : EntityClass<UUID, Verification>(VerificationTable, Verification::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): Verification {
            return Verification(entityId)
        }
    }

    fun approve(reason: String? = null) {
        if (status == Status.PENDING || status == Status.WAITING) {
            status = Status.APPROVED
            this.reason = reason
            changedAt = LocalDateTime.now()
            updateLastData()
            addEvent(VerificationApproved(_player, type))
        } else {
            throw IncorrectVerificationStatusException("Incorrect verification status $status to approve")
        }
    }

    fun decline(reason: String? = null) {
        if (status == Status.PENDING) {
            status = Status.DECLINED
            this.reason = reason
            changedAt = LocalDateTime.now()
            updateLastData()
            addEvent(VerificationDeclined(_player, type))
        } else {
            throw IncorrectVerificationStatusException("Incorrect verification status $status to decline")
        }
    }

    private fun updateLastData() {
        val current = data.last() as JsonObject
        val map = current.toMutableMap()
        map["reason"] = JsonPrimitive(reason)
        map["status"] = JsonPrimitive(status.name)
        _data = _data.dropLast(1) + listOf(JsonObject(map))
    }

    fun isPassed(): Boolean {
        return status == Status.APPROVED
    }

    fun isHold(): Boolean {
        return status == Status.PENDING
    }

    inline fun <reified T : VerificationData> appendData(info: T) {
        val element = json.encodeToJsonElement(T::class.serializer(), info)
        appendData(element)
    }

    inline fun <reified T : VerificationData> lastData(): T? {
        if (data.isEmpty()) {
            return null
        }
        return json.decodeFromJsonElement(T::class.serializer(), data.last())
    }

    fun appendData(value: JsonElement) {
        val data = _data.toMutableList()
        if (status == Status.PENDING && data.isNotEmpty()) {
            data[data.lastIndex] = value
        } else {
            status = Status.PENDING
            data.add(value)
        }
        _data = data.toList()
    }
}

enum class Status {
    PENDING, APPROVED, DECLINED, WAITING
}

object VerificationTable : UUIDTable("verification_records") {
    val player = uuid("player")
    val type = varchar("type", 25)
    val status = enumerationByName("status", 25, Status::class).default(Status.PENDING)
    val reason = varchar("reason", 255).nullable()
    val data = jsonb("data", ListSerializer(JsonElement.serializer())).default(emptyList())
    val changedAt = datetime("changed_at").clientDefault { LocalDateTime.now() }
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

interface VerificationData {
    val status: Status
    val reason: String?
}
