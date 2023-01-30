package io.betforge.player.application.limits.admin.command

import io.betforge.infrastructure.domain.Money
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class EditMaxBetLimit(
    val amount: Money
) {
    @Transient
    lateinit var limitId: UUID
}
