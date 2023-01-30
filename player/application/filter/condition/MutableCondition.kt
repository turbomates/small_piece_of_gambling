package io.betforge.player.application.filter.condition

import org.jetbrains.exposed.sql.Query

interface MutableCondition : Condition {
    fun mutateQuery(query: Query): Query
}
