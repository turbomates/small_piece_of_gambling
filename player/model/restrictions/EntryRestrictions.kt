package io.betforge.player.model.restrictions

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.exceptions.RestrictionException
import io.betforge.player.model.event.restriction.CoolingOffPeriodCanceled
import io.betforge.player.model.event.restriction.CoolingOffPeriodCreated
import io.betforge.player.model.event.restriction.SelfExclusionPeriodCanceled
import io.betforge.player.model.event.restriction.SelfExclusionPeriodCreated
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

private const val DELAY: Long = 24

sealed class EntryRestrictions(
    id: EntityID<UUID>
) : Entity<UUID>(id), AggregateRoot {

    protected var playerId by EntryRestrictionsTable.playerId
    protected var startedAt by EntryRestrictionsTable.startedAt
    protected var endedAt by EntryRestrictionsTable.endedAt
    protected var type by EntryRestrictionsTable.type

    abstract fun cancel()

    enum class Type {
        COOLING_OFF_PERIOD, SELF_EXCLUSION_PERIOD
    }

    companion object : PrivateEntityClass<UUID, EntryRestrictions>(
        object : EntityClass<UUID, EntryRestrictions>(EntryRestrictionsTable, EntryRestrictions::class.java) {
            private val coolingOffPeriodRepository = object : CoolingOffPeriod.Repository() {}
            private val selfExclusionPeriodRepository = object : SelfExclusionPeriod.Repository() {}
            override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): EntryRestrictions {
                if (row == null) {
                    throw RestrictionException()
                }
                return when (row[EntryRestrictionsTable.type]) {
                    Type.COOLING_OFF_PERIOD -> coolingOffPeriodRepository.wrap(entityId, row)
                    Type.SELF_EXCLUSION_PERIOD -> selfExclusionPeriodRepository.wrap(entityId, row)
                }
            }
        }
    )

    abstract class Repository : EntityClass<UUID, EntryRestrictions>(EntryRestrictionsTable, EntryRestrictions::class.java) {
        private val coolingOffPeriodRepository = object : CoolingOffPeriod.Repository() {}
        private val selfExclusionPeriodRepository = object : SelfExclusionPeriod.Repository() {}
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): EntryRestrictions {
            if (row == null) {
                throw RestrictionException()
            }
            return when (row[EntryRestrictionsTable.type]) {
                Type.COOLING_OFF_PERIOD -> coolingOffPeriodRepository.wrap(entityId, row)
                Type.SELF_EXCLUSION_PERIOD -> selfExclusionPeriodRepository.wrap(entityId, row)
            }
        }
    }
}

class CoolingOffPeriod private constructor(id: EntityID<UUID>) : EntryRestrictions(id) {

    override fun cancel() {
        endedAt = LocalDateTime.now().plusHours(DELAY)
        addEvent(CoolingOffPeriodCanceled(playerId))
    }

    companion object : PrivateEntityClass<UUID, CoolingOffPeriod>(object : Repository() {}) {
        fun create(playerId: UUID, endedAt: LocalDateTime): CoolingOffPeriod {
            val coolingOffPeriod = CoolingOffPeriod.new {
                this.playerId = playerId
                startedAt = LocalDateTime.now()
                type = Type.COOLING_OFF_PERIOD
                this.endedAt = endedAt
            }
            coolingOffPeriod.addEvent(
                CoolingOffPeriodCreated(
                    coolingOffPeriod.playerId
                )
            )
            return coolingOffPeriod
        }
    }

    abstract class Repository : EntityClass<UUID, CoolingOffPeriod>(EntryRestrictionsTable, CoolingOffPeriod::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): CoolingOffPeriod {
            return CoolingOffPeriod(entityId)
        }
    }
}

class SelfExclusionPeriod private constructor(id: EntityID<UUID>) : EntryRestrictions(id) {

    override fun cancel() {
        endedAt = LocalDateTime.now()
        addEvent(SelfExclusionPeriodCanceled(playerId))
    }

    companion object : PrivateEntityClass<UUID, SelfExclusionPeriod>(object : Repository() {}) {
        fun create(playerId: UUID, endedAt: LocalDateTime): SelfExclusionPeriod {
            val selfExclusionPeriod = SelfExclusionPeriod.new {
                this.playerId = playerId
                startedAt = LocalDateTime.now()
                type = Type.SELF_EXCLUSION_PERIOD
                this.endedAt = endedAt
            }
            selfExclusionPeriod.addEvent(
                SelfExclusionPeriodCreated(
                    selfExclusionPeriod.playerId
                )
            )
            return selfExclusionPeriod
        }
    }

    abstract class Repository : EntityClass<UUID, SelfExclusionPeriod>(EntryRestrictionsTable, SelfExclusionPeriod::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): SelfExclusionPeriod {
            return SelfExclusionPeriod(entityId)
        }
    }
}

object EntryRestrictionsTable : UUIDTable("entry_restrictions") {
    val playerId = uuid("player_id")
    val type = enumerationByName("type", 255, EntryRestrictions.Type::class)
    val startedAt = datetime("started_at")
    val endedAt = datetime("ended_at")
}
