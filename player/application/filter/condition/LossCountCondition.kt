@file: UseSerializers(LocalDateTimeSerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.casino.model.statistic.CasinoBetResultStatisticTable
import io.betforge.infrastructure.domain.Currency
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.infrastructure.extensions.validation.isValidRange
import io.betforge.player.infrasturcture.filter.Range
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import org.valiktor.functions.validate
import org.valiktor.functions.validateForEach
import java.time.LocalDateTime
import java.util.UUID

@Serializable
@SerialName("loss_count_condition")
class LossCountCondition constructor(
    val betType: BetType,
    val betBonusType: BetBonusType,
    val matching: Matching,
    val includeCurrentPeriod: Boolean? = null,
    val period: Period? = null,
    val date: @Contextual Range<LocalDateTime>? = null,
    val count: @Contextual Range<Int>? = null,
    val currencyCounts: Map<Currency, @Contextual Range<Int>>? = null,
    val casino: CasinoCondition? = null,
    val sportsbook: SportsbookCondition? = null
) : StrictCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            if (date != null) validate(LossCountCondition::date).isValidRange()
            if (period != null) validate(LossCountCondition::includeCurrentPeriod).isNotNull()
            if (currencyCounts == null) validate(LossCountCondition::count).isNotNull().isValidRange()
            if (count == null) {
                validate(LossCountCondition::currencyCounts).isNotNull().isNotEmpty().validate {
                    validate(Map<Currency, Range<Int>>::entries).validateForEach {
                        validate(Map.Entry<Currency, Range<Int>>::value).isValidRange()
                    }
                }
            }
        }
    }

    override fun strictIds(): Set<UUID> {
        return when (betType) {
            BetType.CASINO -> casinoStrictIds()
            BetType.SPORTSBOOK -> sportsbookStrictIds()
            BetType.ALL -> casinoStrictIds() + sportsbookStrictIds()
        }
    }

    private fun casinoStrictIds(): Set<UUID> {
        if (currencyCounts.isNullOrEmpty()) {
            return casinoQuery(count!!)
                .map { it[CasinoBetResultStatisticTable.user] }
                .toSet()
        }
        return currencyCounts.toList().foldIndexed(setOf()) { index, ids: Set<UUID>, (currency, count) ->
            val queryIds = casinoQuery(count, currency)
                .map { it[CasinoBetResultStatisticTable.user] }
                .toSet()
            when (matching) {
                Matching.ALL -> if (index == 0 && ids.isEmpty()) queryIds else ids.intersect(queryIds)
                Matching.ANY -> ids + queryIds
            }
        }
    }

    // #TODO Need to implement
    private fun sportsbookStrictIds(): Set<UUID> = emptySet()

    private fun casinoQuery(count: Range<Int>, currency: Currency? = null): Query {
        val dateRange = date ?: period?.toDateTimeRange(LocalDateTime.now(), includeCurrentPeriod!!)

        return CasinoBetResultStatisticTable
            .slice(CasinoBetResultStatisticTable.user)
            .select { CasinoBetResultStatisticTable.winAmount eq 0 }
            .apply {
                currency?.let { andWhere { CasinoBetResultStatisticTable.currency eq it } }
                dateRange?.to?.let { andWhere { CasinoBetResultStatisticTable.createdAt lessEq it } }
                dateRange?.from?.let { andWhere { CasinoBetResultStatisticTable.createdAt greaterEq it } }
                casino?.apply {
                    if (categories != null) andWhere { CasinoBetResultStatisticTable.financeGroup inList categories }
                    if (providers != null) andWhere { CasinoBetResultStatisticTable.provider inList providers }
                    if (games != null) andWhere { CasinoBetResultStatisticTable.gameName inList games }
                }
            }
            .filterBet()
            .filterCount(count)
            .groupBy(CasinoBetResultStatisticTable.user)
    }

    private fun Query.filterBet(): Query {
        return when (betBonusType) {
            BetBonusType.REAL -> andWhere { CasinoBetResultStatisticTable.betBonusAmount eq 0 }
            BetBonusType.BONUS -> andWhere { CasinoBetResultStatisticTable.betBonusAmount greater 0 }
            BetBonusType.ALL -> this
        }
    }

    private fun Query.filterCount(count: Range<Int>): Query {
        return when {
            count.from != null && count.to != null -> having {
                CasinoBetResultStatisticTable.betAmount.count() greaterEq count.from and
                    (CasinoBetResultStatisticTable.betAmount.count() lessEq count.to)
            }
            count.from != null -> having { CasinoBetResultStatisticTable.betAmount.count() greaterEq count.from }
            count.to != null -> having { CasinoBetResultStatisticTable.betAmount.count() lessEq count.to }
            else -> throw UnsupportedOperationException("Cannot filter count with empty range")
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
        val sports: List<String>? = null,
        val categories: List<String>? = null,
        val competitions: List<String>? = null,
        val events: List<String>? = null,
    )
}
