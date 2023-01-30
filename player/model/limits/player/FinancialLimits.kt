package io.betforge.player.model.limits.player

import dev.tmsoft.lib.exposed.dao.AggregateRoot
import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.domain.money
import io.betforge.player.infrasturcture.exceptions.LimitAlreadyExists
import io.betforge.player.infrasturcture.exceptions.LimitException
import io.betforge.player.model.event.limits.player.DepositLimitCanceled
import io.betforge.player.model.event.limits.player.DepositLimitCreated
import io.betforge.player.model.event.limits.player.LossLimitCanceled
import io.betforge.player.model.event.limits.player.LossLimitCreated
import io.betforge.player.model.event.limits.player.WagerLimitCanceled
import io.betforge.player.model.event.limits.player.WagerLimitCreated
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

sealed class FinancialLimits(id: EntityID<UUID>) : Entity<UUID>(id), AggregateRoot {
    protected var playerId by FinancialLimitsTable.playerId
    protected var type by FinancialLimitsTable.type
    protected var period by FinancialLimitsTable.period
    protected var status by FinancialLimitsTable.status
    protected var createdAt by FinancialLimitsTable.createdAt
    protected val limits by MoneyLimit referrersOn LimitsMoneyTable.limitId
    private val usedMoney by UsedLimitsMoney referrersOn UsedLimitsMoneyTable.limitId

    companion object : PrivateEntityClass<UUID, FinancialLimits>(
        object : EntityClass<UUID, FinancialLimits>(FinancialLimitsTable, FinancialLimits::class.java) {
            private val depositLimit = object : DepositLimit.Repository() {}
            private val lossLimit = object : LossLimit.Repository() {}
            private val wagerLimit = object : WagerLimit.Repository() {}
            override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): FinancialLimits {
                if (row == null) {
                    throw LimitException("invalid financial limit type")
                }
                return when (row[FinancialLimitsTable.type]) {
                    Type.DEPOSIT_LIMIT -> depositLimit.wrap(entityId, row)
                    Type.LOSS_LIMIT -> lossLimit.wrap(entityId, row)
                    Type.WAGER_LIMIT -> wagerLimit.wrap(entityId, row)
                }
            }
        }
    )

    abstract fun cancel(currency: Currency, actor: UUID)
    abstract fun edit(newAmount: Money)
    abstract fun add(amount: Money)

    fun period() = period
    fun createdAt(): LocalDateTime = createdAt

    fun isExceeding(withAmount: Money, limitMoney: Money): Boolean {
        val used = usedMoney.find(withAmount.currency)
        val limit = limits.find(withAmount.currency)
        return used != null && limit != null && limit.isActual() && used.isExceeding(withAmount, limitMoney)
    }

    fun use(amount: Money) {
        usedMoney.find(amount.currency)?.use(amount)
    }

    protected fun SizedIterable<MoneyLimit>.find(currency: Currency): MoneyLimit? {
        return find { limit -> limit.amount().currency == currency && limit.endedAt() == null }
    }

    protected fun SizedIterable<UsedLimitsMoney>.find(currency: Currency): UsedLimitsMoney? {
        return find { used -> used.used().currency == currency }
    }

    enum class Type {
        DEPOSIT_LIMIT, LOSS_LIMIT, WAGER_LIMIT
    }

    enum class Status {
        ACTIVE, CLOSED
    }

    enum class Period {
        DAILY, WEEKLY, MONTHLY
    }

    abstract class Repository : EntityClass<UUID, FinancialLimits>(FinancialLimitsTable, FinancialLimits::class.java) {
        private val depositLimitRepository = object : DepositLimit.Repository() {}
        private val lossLimitRepository = object : LossLimit.Repository() {}
        private val wagerLimitRepository = object : WagerLimit.Repository() {}
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): FinancialLimits {
            if (row == null) {
                throw LimitException("invalid financial limit type")
            }
            return when (row[FinancialLimitsTable.type]) {
                Type.DEPOSIT_LIMIT -> depositLimitRepository.wrap(entityId, row)
                Type.LOSS_LIMIT -> lossLimitRepository.wrap(entityId, row)
                Type.WAGER_LIMIT -> wagerLimitRepository.wrap(entityId, row)
            }
        }
    }
}

class DepositLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {

    companion object : PrivateEntityClass<UUID, DepositLimit>(object : Repository() {}) {
        fun create(playerId: UUID, period: Period): DepositLimit {
            val depositLimit = DepositLimit.new {
                this.playerId = playerId
                this.period = period
                type = Type.DEPOSIT_LIMIT
                status = Status.ACTIVE
            }
            depositLimit.addEvent(
                DepositLimitCreated(
                    depositLimit.playerId
                )
            )
            return depositLimit
        }
    }

    override fun add(amount: Money) {
        if (limits.find(amount.currency) != null) throw LimitAlreadyExists(amount.currency)
        MoneyLimit.new(this, amount)
        addEvent(DepositLimitCreated(playerId))
    }

    override fun edit(
        newAmount: Money
    ) {
        val limit = limits.find(newAmount.currency)
        if (limit == null) {
            MoneyLimit.new(this, newAmount)
        } else {
            limit.edit(newAmount, this)
        }
    }

    override fun cancel(currency: Currency, actor: UUID) {
        val limit = limits.find(currency)
        if (actor == playerId && limit != null) {
            limit.cancel(MoneyLimit.delay())
            status = Status.CLOSED
        } else if (actor != playerId && limit != null) {
            limit.cancel(LocalDateTime.now())
            status = Status.CLOSED
        }
        addEvent(DepositLimitCanceled(playerId))
    }

    abstract class Repository : EntityClass<UUID, DepositLimit>(FinancialLimitsTable, DepositLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): DepositLimit {
            return DepositLimit(entityId)
        }
    }
}

class LossLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {

    companion object : PrivateEntityClass<UUID, LossLimit>(object : Repository() {}) {
        fun create(playerId: UUID, period: Period): LossLimit {
            val lossLimit = LossLimit.new {
                this.playerId = playerId
                this.period = period
                type = Type.LOSS_LIMIT
                status = Status.ACTIVE
            }
            lossLimit.addEvent(
                LossLimitCreated(
                    lossLimit.playerId
                )
            )
            return lossLimit
        }
    }

    override fun add(amount: Money) {
        if (limits.find(amount.currency) != null) throw throw LimitAlreadyExists(amount.currency)
        MoneyLimit.new(this, amount)
        addEvent(LossLimitCreated(playerId))
    }

    override fun edit(newAmount: Money) {
        val limit = limits.find(newAmount.currency)
        if (limit == null) {
            MoneyLimit.new(this, newAmount)
        } else {
            limit.edit(newAmount, this)
        }
    }

    override fun cancel(currency: Currency, actor: UUID) {
        val limit = limits.find(currency)
        if (actor == playerId && limit != null) {
            limit.cancel(MoneyLimit.delay())
            status = Status.CLOSED
        } else if (actor != playerId && limit != null) {
            limit.cancel(LocalDateTime.now())
            status = Status.CLOSED
        }
        addEvent(LossLimitCanceled(playerId))
    }

    abstract class Repository : EntityClass<UUID, LossLimit>(FinancialLimitsTable, LossLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): LossLimit {
            return LossLimit(entityId)
        }
    }
}

class WagerLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {

    companion object : PrivateEntityClass<UUID, WagerLimit>(object : Repository() {}) {
        fun create(playerId: UUID, period: Period): WagerLimit {
            val wagerLimit = WagerLimit.new {
                this.playerId = playerId
                this.period = period
                type = Type.WAGER_LIMIT
                status = Status.ACTIVE
            }
            wagerLimit.addEvent(
                WagerLimitCreated(
                    wagerLimit.playerId
                )
            )
            return wagerLimit
        }
    }

    override fun add(amount: Money) {
        if (limits.find(amount.currency) != null) throw LimitAlreadyExists(amount.currency)
        MoneyLimit.new(this, amount)
        addEvent(WagerLimitCreated(playerId))
    }

    override fun edit(newAmount: Money) {
        val limit = limits.find(newAmount.currency)
        if (limit == null) {
            MoneyLimit.new(this, newAmount)
        } else {
            limit.edit(newAmount, this)
        }
    }

    override fun cancel(currency: Currency, actor: UUID) {
        val limit = limits.find(currency)
        if (actor == playerId && limit != null) {
            limit.cancel(MoneyLimit.delay())
            status = Status.CLOSED
        } else if (actor != playerId && limit != null) {
            limit.cancel(LocalDateTime.now())
            status = Status.CLOSED
        }
        addEvent(WagerLimitCanceled(playerId))
    }

    abstract class Repository : EntityClass<UUID, WagerLimit>(FinancialLimitsTable, WagerLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): WagerLimit {
            return WagerLimit(entityId)
        }
    }
}

class MoneyLimit(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var limit by FinancialLimits referencedOn LimitsMoneyTable.limitId
    private var amount by LimitsMoneyTable.money
    private var startedAt by LimitsMoneyTable.startedAt
    private var endedAt by LimitsMoneyTable.endedAt

    companion object : EntityClass<UUID, MoneyLimit>(LimitsMoneyTable, MoneyLimit::class.java) {
        fun new(limit: FinancialLimits, amount: Money, startedAt: LocalDateTime = LocalDateTime.now()): MoneyLimit {
            UsedLimitsMoney.create(amount.zero(), limit)
            return new {
                this.limit = limit
                this.amount = amount
                this.startedAt = startedAt
            }
        }

        private const val DELAY: Long = 24
        fun delay(): LocalDateTime {
            return LocalDateTime.now().plusHours(DELAY)
        }
    }

    abstract class Repository : EntityClass<UUID, MoneyLimit>(LimitsMoneyTable, MoneyLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): MoneyLimit {
            return MoneyLimit(entityId)
        }
    }

    fun amount(): Money = amount
    fun endedAt(): LocalDateTime? = endedAt

    fun edit(newAmount: Money, limitId: FinancialLimits) {
        if (newAmount > amount) {
            cancel(delay())
        } else {
            cancel(LocalDateTime.now())
        }
        new(limitId, newAmount, endedAt!!)
    }

    fun cancel(date: LocalDateTime) {
        endedAt = date
    }

    fun isActual(): Boolean {
        return startedAt < LocalDateTime.now() && endedAt == null
    }
}

class UsedLimitsMoney(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var used by UsedLimitsMoneyTable.used
    private var limit by FinancialLimits referencedOn UsedLimitsMoneyTable.limitId
    private var calculatedFrom by UsedLimitsMoneyTable.calculatedFrom

    companion object : EntityClass<UUID, UsedLimitsMoney>(UsedLimitsMoneyTable, UsedLimitsMoney::class.java) {
        fun create(usedMoney: Money, limit: FinancialLimits): UsedLimitsMoney {
            return new {
                this.limit = limit
                this.used = usedMoney
                this.calculatedFrom = limit.createdAt()
            }
        }
    }

    fun used(): Money = used
    fun use(amount: Money) {
        used += amount
    }

    fun isExceeding(amount: Money, moneyLimit: Money): Boolean {
        resetUsedAmount()
        return amount.plus(used) > moneyLimit
    }

    private fun resetUsedAmount() {
        if (calculatedFrom.nextPeriod() < LocalDateTime.now()) {
            calculatedFrom = calculatedFrom.nextPeriod()
            used = used.zero()
        }
    }

    private fun LocalDateTime.nextPeriod(): LocalDateTime {
        return when (limit.period()) {
            FinancialLimits.Period.DAILY -> plusDays(1)
            FinancialLimits.Period.WEEKLY -> plusWeeks(1)
            FinancialLimits.Period.MONTHLY -> plusMonths(1)
        }
    }
}

object FinancialLimitsTable : UUIDTable("financial_limits") {
    val playerId = uuid("player_id")
    val type = enumerationByName("type", 255, FinancialLimits.Type::class)
    val period = enumerationByName("period", 255, FinancialLimits.Period::class)
    val status = enumerationByName("status", 255, FinancialLimits.Status::class)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object LimitsMoneyTable : UUIDTable("limits_money") {
    val limitId = reference("limit_id", FinancialLimitsTable)
    val money = money("limit_")
    val startedAt = datetime("started_at").clientDefault { LocalDateTime.now() }
    val endedAt = datetime("ended_at").nullable().default(null)
}

object UsedLimitsMoneyTable : UUIDTable("used_limits_money") {
    val used = money("used_")
    val calculatedFrom = datetime("calculated_from")
    val limitId = reference("limit_id", FinancialLimitsTable.id)
}
