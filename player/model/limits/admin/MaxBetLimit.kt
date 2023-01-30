package io.betforge.player.model.limits.admin

import dev.tmsoft.lib.exposed.dao.PrivateEntityClass
import io.betforge.player.infrasturcture.limits.LimitRelation
import io.betforge.player.model.event.limits.admin.DepositLimitCreated
import io.betforge.player.model.event.limits.admin.MaxBetLimitDeleted
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

class MaxBetLimit private constructor(id: EntityID<UUID>) : FinancialLimits(id) {
    companion object : PrivateEntityClass<UUID, MaxBetLimit>(object : MaxBetLimit.Repository() {}) {
        fun create(relation: LimitRelation): MaxBetLimit {
            return MaxBetLimit.new {
                this.period = Period.NONE
                this.type = Type.MAX_BET_LIMIT
            }.apply {
                createLimitRelation(relation)
                addEvent(DepositLimitCreated(relation))
            }
        }
    }

    override fun remove() {
        val relation = relation()
        removeRelations()
        addEvent(MaxBetLimitDeleted(relation))
    }

    abstract class Repository : EntityClass<UUID, MaxBetLimit>(AdminFinancialLimitsTable, MaxBetLimit::class.java) {
        override fun createInstance(entityId: EntityID<UUID>, row: ResultRow?): MaxBetLimit {
            return MaxBetLimit(entityId)
        }
    }
}
