@file:UseSerializers(UUIDSerializer::class, LocalDateTimeSerializer::class)

package io.betforge.player.application.limits.admin.query

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.domain.Money
import io.betforge.player.model.limits.admin.AdminFinancialLimitsTable
import io.betforge.player.model.limits.admin.AdminMoneyLimitsTable
import io.betforge.player.model.limits.admin.FinancialLimits
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class FinancialLimit(
    val id: UUID,
    val type: FinancialLimits.Type,
    val period: FinancialLimits.Period,
    val amount: Money,
    val createdAt: LocalDateTime
)

fun ResultRow.toFinancialLimit() = FinancialLimit(
    this[AdminFinancialLimitsTable.id].value,
    this[AdminFinancialLimitsTable.type],
    this[AdminFinancialLimitsTable.period],
    this[AdminMoneyLimitsTable.money],
    this[AdminFinancialLimitsTable.createdAt]
)
