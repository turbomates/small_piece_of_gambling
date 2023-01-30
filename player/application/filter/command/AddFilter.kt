package io.betforge.player.application.filter.command

import io.betforge.player.application.filter.condition.Condition
import kotlinx.serialization.Serializable

@Serializable
data class AddFilter(
    val name: String,
    val conditions: List<Condition>
)
