package io.betforge.player.infrasturcture.limits.query

import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.limits.player.FinancialLimits
import java.time.LocalDateTime
import java.util.UUID

data class PlayerLimit(
    val id: UUID,
    val period: FinancialLimits.Period,
    val limitMoney: Money,
    val limitStartedAt: LocalDateTime,
    val limitEndedAt: LocalDateTime?,
    val usedMoneyId: UUID,
    val usedMoney: Money,
    val calculatedFrom: LocalDateTime
)
