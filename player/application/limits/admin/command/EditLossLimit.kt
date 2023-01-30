package io.betforge.player.application.limits.admin.command

import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.limits.admin.FinancialLimits
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
data class EditLossLimit(
    val period: FinancialLimits.Period,
    val amount: Money
) {
    @Transient
    lateinit var limitId: UUID
}
