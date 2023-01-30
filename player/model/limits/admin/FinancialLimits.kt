package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.infrastructure.domain.Money
import io.betforge.player.infrasturcture.exceptions.InvalidFinancialLimitType
import io.betforge.player.infrasturcture.exceptions.LimitException
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.infrasturcture.limits.RelationType
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

sealed class FinancialLimits(id: EntityID<UUID>) : Entity<UUID>(id), AggregateRoot {
    protected var type by AdminFinancialLimitsTable.type
    protected var period by AdminFinancialLimitsTable.period
    protected var createdAt by AdminFinancialLimitsTable.createdAt
    protected var updatedAt by AdminFinancialLimitsTable.updatedAt
    private val moneyLimit by MoneyLimit backReferencedOn AdminMoneyLimitsTable.limitId
    protected val group by AdminGroupsLimit optionalBackReferencedOn AdminGroupsLimitsTable.limitId
    protected val player by AdminPlayersLimit optionalBackReferencedOn AdminPlayersLimitsTable.limitId
    protected val application by AdminApplicationLimit optionalBackReferencedOn AdminApplicationLimitsTable.limitId

    companion object : PrivateEntityClass<UUID, FinancialLimits>(
        object : EntityClass<UUID, FinancialLimits>(AdminFinancialLimitsTable, FinancialLimits::class.java) {
            private val depositLimit = object : DepositLimit.Repository() {}
            private val lossLimit = object : LossLimit.Repository() {}
            private val wagerLimit = object : WagerLimit.Repository() {}
            private val maxBetLimit = object : MaxBetLimit.Repository() {}
            private val withdrawLimit = object : WithdrawLimit.Repository() {}
            override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): FinancialLimits {
                if (row == null) throw InvalidFinancialLimitType()
                return when (row[AdminFinancialLimitsTable.type]) {
                    Type.DEPOSIT_LIMIT -> depositLimit.wrap(entityId, row)
                    Type.LOSS_LIMIT -> lossLimit.wrap(entityId, row)
                    Type.WAGER_LIMIT -> wagerLimit.wrap(entityId, row)
                    Type.MAX_BET_LIMIT -> maxBetLimit.wrap(entityId, row)
                    Type.WITHDRAW_LIMIT -> withdrawLimit.wrap(entityId, row)
                }
            }
        }
    )

    abstract fun remove()

    fun edit(newAmount: Money, newPeriod: Period) {
        period = newPeriod
        updatedAt = LocalDateTime.now()
        moneyLimit.edit(newAmount)
    }

    protected fun createLimitRelation(relation: LimitRelation) {
        when (relation.type) {
            RelationType.PLAYER -> AdminPlayersLimit.create(this, relation.id)
            RelationType.GROUP -> AdminGroupsLimit.create(this, relation.id)
            RelationType.APPLICATION -> AdminApplicationLimit.create(this, relation.id)
        }
    }

    fun relation(): LimitRelation = when {
        player != null -> LimitRelation(player!!.id(), RelationType.PLAYER)
        group != null -> LimitRelation(group!!.id(), RelationType.GROUP)
        application != null -> LimitRelation(application!!.id(), RelationType.APPLICATION)
        else -> throw LimitException("Have no limits relations: ${id.value}")
    }

    fun removeRelations() {
        moneyLimit.delete()
        application?.delete()
        player?.delete()
        group?.delete()
        delete()
    }

    enum class Type {
        DEPOSIT_LIMIT, LOSS_LIMIT, WAGER_LIMIT, MAX_BET_LIMIT, WITHDRAW_LIMIT
    }

    enum class Period {
        DAILY, WEEKLY, MONTHLY, NONE
    }

    abstract class Repository :
        EntityClass<UUID, FinancialLimits>(AdminFinancialLimitsTable, FinancialLimits::class.java) {
        private val depositLimitRepository = object : DepositLimit.Repository() {}
        private val lossLimitRepository = object : LossLimit.Repository() {}
        private val wagerLimitRepository = object : WagerLimit.Repository() {}
        private val maxBetLimitRepository = object : MaxBetLimit.Repository() {}
        private val withdrawLimitRepository = object : WithdrawLimit.Repository() {}
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): FinancialLimits {
            if (row == null) throw InvalidFinancialLimitType()
            return when (row[AdminFinancialLimitsTable.type]) {
                Type.LOSS_LIMIT -> lossLimitRepository.wrap(entityId, row)
                Type.WAGER_LIMIT -> wagerLimitRepository.wrap(entityId, row)
                Type.MAX_BET_LIMIT -> maxBetLimitRepository.wrap(entityId, row)
                Type.DEPOSIT_LIMIT -> depositLimitRepository.wrap(entityId, row)
                Type.WITHDRAW_LIMIT -> withdrawLimitRepository.wrap(entityId, row)
            }
        }
    }
}

class AdminPlayersLimit(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var limitId by FinancialLimits referencedOn AdminPlayersLimitsTable.limitId
    private var playerId by AdminPlayersLimitsTable.playerId

    companion object : PrivateEntityClass<UUID, AdminPlayersLimit>(
        object : EntityClass<UUID, AdminPlayersLimit>(AdminPlayersLimitsTable, AdminPlayersLimit::class.java) {}
    ) {
        fun create(
            limitId: FinancialLimits,
            playerId: UUID
        ): AdminPlayersLimit = new {
            this.limitId = limitId
            this.playerId = playerId
        }
    }

    fun id(): UUID = id.value

    abstract class Repository : EntityClass<UUID, AdminPlayersLimit>(AdminPlayersLimitsTable, AdminPlayersLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): AdminPlayersLimit {
            return AdminPlayersLimit(entityId)
        }
    }
}

class AdminGroupsLimit(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var limitId by FinancialLimits referencedOn AdminGroupsLimitsTable.limitId
    private var playerGroupId by AdminGroupsLimitsTable.playerGroupId

    companion object : PrivateEntityClass<UUID, AdminGroupsLimit>(
        object : EntityClass<UUID, AdminGroupsLimit>(AdminGroupsLimitsTable, AdminGroupsLimit::class.java) {}
    ) {
        fun create(
            limitId: FinancialLimits,
            playerId: UUID
        ): AdminGroupsLimit = new {
            this.limitId = limitId
            this.playerGroupId = playerId
        }
    }

    fun id(): UUID = id.value

    abstract class Repository : EntityClass<UUID, AdminGroupsLimit>(AdminGroupsLimitsTable, AdminGroupsLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): AdminGroupsLimit {
            return AdminGroupsLimit(entityId)
        }
    }
}

class AdminApplicationLimit(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var limitId by FinancialLimits referencedOn AdminApplicationLimitsTable.limitId
    private var applicationId by AdminApplicationLimitsTable.application

    companion object : PrivateEntityClass<UUID, AdminApplicationLimit>(
        object : EntityClass<UUID, AdminApplicationLimit>(AdminApplicationLimitsTable, AdminApplicationLimit::class.java) {}
    ) {
        fun create(
            limitId: FinancialLimits,
            application: UUID
        ): AdminApplicationLimit = new {
            this.limitId = limitId
            this.applicationId = application
        }
    }

    fun id(): UUID = id.value

    abstract class Repository : EntityClass<UUID, AdminApplicationLimit>(AdminApplicationLimitsTable, AdminApplicationLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): AdminApplicationLimit {
            return AdminApplicationLimit(entityId)
        }
    }
}

object AdminFinancialLimitsTable : UUIDTable("admin_financial_limits") {
    val type = enumerationByName("type", 255, FinancialLimits.Type::class)
    val period = enumerationByName("period", 255, FinancialLimits.Period::class)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object AdminPlayersLimitsTable : UUIDTable("admin_players_limits") {
    val limitId = reference("limit_id", AdminFinancialLimitsTable)
    val playerId = uuid("player_id")
}

object AdminGroupsLimitsTable : UUIDTable("admin_groups_limits") {
    val limitId = reference("limit_id", AdminFinancialLimitsTable)
    val playerGroupId = uuid("players_group_id")
}

object AdminApplicationLimitsTable : UUIDTable("admin_application_limits") {
    val limitId = reference("limit_id", AdminFinancialLimitsTable)
    val application = uuid("application_id")
}
