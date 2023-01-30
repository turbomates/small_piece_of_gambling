package io.betforge.player.application.filter.condition

import java.util.UUID

interface StrictCondition : Condition {
    fun strictIds(): Set<UUID>
}
