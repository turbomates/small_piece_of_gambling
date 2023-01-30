package io.betforge.player.application.limits.player.command

import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.serializer.MoneySerializer
import io.betforge.player.model.limits.player.FinancialLimits
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class EditLimit(
    @Serializable(with = MoneySerializer::class)
    val money: Money,
    val period: FinancialLimits.Period
) {
    @Transient
    lateinit var playerId: UUID
}
