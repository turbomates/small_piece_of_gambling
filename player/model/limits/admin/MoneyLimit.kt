package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.domain.money
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class MoneyLimit(id: EntityID<UUID>) : Entity<UUID>(id) {
    private var amount by AdminMoneyLimitsTable.money
    private var limit by FinancialLimits referencedOn AdminMoneyLimitsTable.limitId

    companion object : PrivateEntityClass<UUID, MoneyLimit>(
        object : EntityClass<UUID, MoneyLimit>(AdminMoneyLimitsTable, MoneyLimit::class.java) {}
    ) {
        fun new(limit: FinancialLimits, amount: Money): MoneyLimit {
            return new {
                this.limit = limit
                this.amount = amount
            }
        }
    }

    fun amount(): Money = amount

    fun edit(newAmount: Money) {
        this.amount = newAmount
    }

    abstract class Repository : EntityClass<UUID, MoneyLimit>(AdminMoneyLimitsTable, MoneyLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): MoneyLimit {
            return MoneyLimit(entityId)
        }
    }
}

object AdminMoneyLimitsTable : UUIDTable("admin_limits_money") {
    val money = money("limit_")
    val limitId = reference("limit_id", AdminFinancialLimitsTable)
}
