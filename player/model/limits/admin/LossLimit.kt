package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.event.limits.admin.DepositLimitCreated
import io.betforge.player.model.event.limits.admin.LossLimitDeleted
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class LossLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {
    companion object : PrivateEntityClass<UUID, LossLimit>(object : LossLimit.Repository() {}) {
        fun create(
            period: Period,
            relation: LimitRelation
        ): LossLimit {
            return LossLimit.new {
                this.period = period
                this.type = Type.LOSS_LIMIT
            }.apply {
                createLimitRelation(relation)
                addEvent(DepositLimitCreated(relation))
            }
        }
    }

    override fun remove() {
        val relation = relation()
        removeRelations()
        addEvent(LossLimitDeleted(relation))
    }

    abstract class Repository : EntityClass<UUID, LossLimit>(AdminFinancialLimitsTable, LossLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): LossLimit {
            return LossLimit(entityId)
        }
    }
}
