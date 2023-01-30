package io.betforge.player.model.event.limits.admin

import dev.tmsoft.lib.event.Event
import io.betforge.player.infrasturcture.limits.LimitRelation
import kotlinx.serialization.Serializable

@Serializable
data class WithdrawLimitCreated(
    val relation: LimitRelation
) : Event() {

    override val key: Key<out Event>
        get() = Companion

    companion object : Key<WagerLimitDeleted>
}
