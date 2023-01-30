@file:UseSerializers(UUIDSerializer::class, LocalDateTimeSerializer::class, MoneySerializer::class)

package io.betforge.player.application.limits.player.queryobject

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.exposed.query.QueryObject
import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.serializer.MoneySerializer
import io.betforge.player.model.limits.player.FinancialLimits.Period
import io.betforge.player.model.limits.player.FinancialLimitsTable
import io.betforge.player.model.limits.player.LimitsMoneyTable
import io.betforge.player.model.limits.player.UsedLimitsMoneyTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.UUID

class FinancialLimitsQO(
    private val playerId: UUID
) : QueryObject<List<FinancialLimits>> {
    override suspend fun getData(): List<FinancialLimits> {
        return FinancialLimitsTable
            .join(LimitsMoneyTable, JoinType.LEFT, FinancialLimitsTable.id, LimitsMoneyTable.limitId)
            .join(UsedLimitsMoneyTable, JoinType.LEFT, FinancialLimitsTable.id, UsedLimitsMoneyTable.limitId)
            .select {
                FinancialLimitsTable.playerId eq playerId and
                    (LimitsMoneyTable.endedAt.greater(LocalDateTime.now()) or (LimitsMoneyTable.endedAt eq null))
            }
            .orderBy(LimitsMoneyTable.startedAt, SortOrder.ASC)
            .toFinancialLimitsList()
    }
}

fun ResultRow.toFinancialLimits() = FinancialLimits(
    this[FinancialLimitsTable.id].value,
    this[FinancialLimitsTable.playerId],
    this[FinancialLimitsTable.type].name,
    this[FinancialLimitsTable.period],
    this[FinancialLimitsTable.createdAt],
    emptyList()
)

fun ResultRow.toMoneyLimits() = MoneyLimits(
    this[LimitsMoneyTable.id].value,
    this[LimitsMoneyTable.money],
    this[LimitsMoneyTable.startedAt],
    this[LimitsMoneyTable.endedAt],
    this.getOrNull(UsedLimitsMoneyTable.used) ?: this[LimitsMoneyTable.money].zero()
)

fun Iterable<ResultRow>.toFinancialLimitsList(): List<FinancialLimits> {
    return fold(mutableMapOf<UUID, FinancialLimits>()) { map, resultRow ->
        val financialLimits = resultRow.toFinancialLimits()
        val current = map.getOrDefault(financialLimits.id, financialLimits)
        val moneyLimitId = resultRow.getOrNull(LimitsMoneyTable.id)
        val moneyLimits = moneyLimitId?.let {
            if (current.moneyLimits.any { limit -> limit.moneyLimitId == moneyLimitId.value }) null else resultRow.toMoneyLimits()
        }

        map[financialLimits.id] = current.copy(
            moneyLimits = current.moneyLimits.plus(listOfNotNull(moneyLimits))
        )
        map
    }.values.toList()
}

@Serializable
data class FinancialLimits(
    val id: UUID,
    val playerId: UUID,
    val type: String,
    val period: Period,
    val startedAt: LocalDateTime,
    val moneyLimits: List<MoneyLimits>
)

@Serializable
data class MoneyLimits(
    val moneyLimitId: UUID,
    val limit: Money,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime?,
    val used: Money
)
