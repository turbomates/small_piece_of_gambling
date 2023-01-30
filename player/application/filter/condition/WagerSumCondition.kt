@file:UseSerializers(LocalDateTimeSerializer::class, FilterMoneySerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.validation.Error
import io.betforge.casino.model.statistic.CasinoBetResultStatisticTable
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.infrastructure.extensions.validation.isValidMoneyRange
import io.betforge.infrastructure.extensions.validation.isValidRange
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.infrasturcture.serializer.FilterMoneySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import java.time.LocalDateTime
import java.util.UUID
import dev.tmsoft.lib.ktor.validate as ktorValidate

@Serializable
@SerialName("wager_sum_condition")
class WagerSumCondition constructor(
    val betType: BetType,
    private val betBonusType: BetBonusType,
    val balances: List<@Contextual Range<Money>>,
    val matching: Matching,
    val includeCurrentPeriod: Boolean? = null,
    @Contextual
    val date: Range<LocalDateTime>? = null,
    val period: Period? = null,
    val casino: CasinoCondition? = null,
    val sportsbook: SportsbookCondition? = null
) : StrictCondition {
    override suspend fun validate(): List<Error> {
        return ktorValidate(this) {
            validate(WagerSumCondition::balances).isNotEmpty().isValidMoneyRange()
            if (date != null) validate(WagerSumCondition::date).isValidRange()
            if (period != null) validate(WagerSumCondition::includeCurrentPeriod).isNotNull()
        }
    }

    override fun strictIds(): Set<UUID> {
        return when (betType) {
            BetType.CASINO -> casinoStrictIds()
            BetType.SPORTSBOOK -> sportsbookStrictIds()
            BetType.ALL -> casinoStrictIds() + sportsbookStrictIds()
        }
    }

    fun casinoStrictIds(): Set<UUID> {
        val dateRange = date ?: period?.toDateTimeRange(LocalDateTime.now(), includeCurrentPeriod!!)
        return balances.foldIndexed(setOf()) { index, ids: Set<UUID>, balanceRange ->
            val queryIds = casinoQuery(dateRange, balanceRange).map { it[CasinoBetResultStatisticTable.user] }.toSet()
            when (matching) {
                Matching.ALL -> if (index == 0 && ids.isEmpty()) queryIds else ids.intersect(queryIds)
                Matching.ANY -> ids + queryIds
            }
        }
    }

    // TODO Need to implement
    fun sportsbookStrictIds(): Set<UUID> = emptySet()

    private fun casinoQuery(dateRange: Range<LocalDateTime>?, balanceRange: Range<Money>): Query {
        val currency = balanceRange.from?.currency ?: balanceRange.to?.currency
        return CasinoBetResultStatisticTable
            .slice(CasinoBetResultStatisticTable.user)
            .selectAll()
            .apply { dateRange?.to?.let { andWhere { CasinoBetResultStatisticTable.createdAt lessEq it } } }
            .apply { dateRange?.from?.let { andWhere { CasinoBetResultStatisticTable.createdAt greaterEq it } } }
            .andWhere { CasinoBetResultStatisticTable.currency eq currency!! }
            .casinoFilterBet()
            .apply {
                casino?.apply {
                    if (categories != null) andWhere { CasinoBetResultStatisticTable.financeGroup inList categories }
                    if (providers != null) andWhere { CasinoBetResultStatisticTable.provider inList providers }
                    if (games != null) andWhere { CasinoBetResultStatisticTable.gameName inList games }
                }
            }
            .groupBy(CasinoBetResultStatisticTable.user, CasinoBetResultStatisticTable.currency)
            .casinoFilterSum(balanceRange)
    }

    private fun Query.casinoFilterBet(): Query {
        return when (betBonusType) {
            BetBonusType.REAL -> andWhere { CasinoBetResultStatisticTable.betBonusAmount eq 0 }
            BetBonusType.BONUS -> andWhere { CasinoBetResultStatisticTable.betBonusAmount greater 0 }
            BetBonusType.ALL -> this
        }
    }

    private fun Query.casinoFilterSum(balanceRange: Range<Money>): Query {
        return when {
            balanceRange.from != null && balanceRange.to !== null ->
                having {
                    CasinoBetResultStatisticTable.betAmount.sum() greaterEq balanceRange.from.amount and
                        (CasinoBetResultStatisticTable.betAmount.sum() lessEq balanceRange.to.amount)
                }
            balanceRange.from != null ->
                having { CasinoBetResultStatisticTable.betAmount.sum() greaterEq balanceRange.from.amount }
            balanceRange.to != null ->
                having { CasinoBetResultStatisticTable.betAmount.sum() lessEq balanceRange.to.amount }
            else -> throw UnsupportedOperationException("Cannot filter balance with empty range")
        }
    }

    @Serializable
    enum class Matching {
        ALL,
        ANY
    }

    @Serializable
    enum class BetType {
        CASINO,
        SPORTSBOOK,
        ALL
    }

    @Serializable
    enum class BetBonusType {
        BONUS,
        REAL,
        ALL
    }

    @Serializable
    data class CasinoCondition(
        val categories: List<String>? = null,
        val providers: List<String>? = null,
        val games: List<String>? = null,
    )

    @Serializable
    data class SportsbookCondition(
        val sports: List<Long>? = null,
        val categories: List<Long>? = null,
        val competitions: List<Long>? = null,
        val events: List<Long>? = null,
    )
}
