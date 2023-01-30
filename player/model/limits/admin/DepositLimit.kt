package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.event.limits.admin.DepositLimitCreated
import io.betforge.player.model.event.limits.admin.DepositLimitDeleted
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class DepositLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {
    companion object : PrivateEntityClass<UUID, DepositLimit>(object : DepositLimit.Repository() {}) {
        fun create(
            period: Period,
            relation: LimitRelation
        ): DepositLimit {
            return DepositLimit.new {
                this.period = period
                this.type = Type.DEPOSIT_LIMIT
            }.apply {
                createLimitRelation(relation)
                addEvent(DepositLimitCreated(relation))
            }
        }
    }

    override fun remove() {
        val relation = relation()
        removeRelations()
        addEvent(DepositLimitDeleted(relation))
    }

    abstract class Repository : EntityClass<UUID, DepositLimit>(AdminFinancialLimitsTable, DepositLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): DepositLimit {
            return DepositLimit(entityId)
        }
    }
}
