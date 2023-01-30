package io.betforge.player.application.limits.player.command

import io.betforge.infrastructure.domain.Currency
import io.betforge.player.model.limits.player.FinancialLimits
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class CancelLimit(
    val currency: Currency,
    val period: FinancialLimits.Period
) {
    @Transient
    lateinit var playerId: UUID
}
