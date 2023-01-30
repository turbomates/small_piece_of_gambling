package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.event.limits.admin.DepositLimitCreated
import io.betforge.player.model.event.limits.admin.WagerLimitDeleted
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class WagerLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {
    companion object : PrivateEntityClass<UUID, WagerLimit>(object : WagerLimit.Repository() {}) {
        fun create(
            period: Period,
            relation: LimitRelation
        ): WagerLimit {
            return WagerLimit.new {
                this.period = period
                this.type = Type.WAGER_LIMIT
            }.apply {
                createLimitRelation(relation)
                addEvent(DepositLimitCreated(relation))
            }
        }
    }

    override fun remove() {
        val relation = relation()
        removeRelations()
        addEvent(WagerLimitDeleted(relation))
    }

    abstract class Repository : EntityClass<UUID, WagerLimit>(AdminFinancialLimitsTable, WagerLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): WagerLimit {
            return WagerLimit(entityId)
        }
    }
}
