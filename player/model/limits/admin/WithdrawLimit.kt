package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.event.limits.admin.DepositLimitCreated
import io.betforge.player.model.event.limits.admin.WithdrawLimitDeleted
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class WithdrawLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {
    companion object : PrivateEntityClass<UUID, WithdrawLimit>(object : WithdrawLimit.Repository() {}) {
        fun create(
            period: Period,
            relation: LimitRelation
        ): WithdrawLimit {
            return WithdrawLimit.new {
                this.period = period
                this.type = Type.WITHDRAW_LIMIT
            }.apply {
                createLimitRelation(relation)
                addEvent(DepositLimitCreated(relation))
            }
        }
    }

    override fun remove() {
        val relation = relation()
        removeRelations()
        addEvent(WithdrawLimitDeleted(relation))
    }

    abstract class Repository : EntityClass<UUID, WithdrawLimit>(AdminFinancialLimitsTable, WithdrawLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): WithdrawLimit {
            return WithdrawLimit(entityId)
        }
    }
}
