package io.betforge.player.application.filter.command

import io.betforge.player.application.filter.condition.Condition
import kotlinx.serialization.Serializable

@Serializable
data class EditFilter(
    val name: String,
    val newName: String? = null,
    val conditions: List<Condition>
)
