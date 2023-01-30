package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.query.filter.addJoin
import dev.tmsoft.lib.validation.Error
import io.betforge.casino.model.statistic.CasinoBetResultStatisticTable
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.valiktor.functions.isFalse
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isTrue
import java.time.LocalDateTime

@Serializable
@SerialName("lack_bets_condition")
class LackBetsCondition(
    val period: Period? = null,
    val includeCurrentPeriod: Boolean? = null,
    val lifetime: Boolean
) : MutableCondition {
    override suspend fun validate(): List<Error> {
        return validate(this) {
            if (period != null) {
                validate(LackBetsCondition::includeCurrentPeriod).isNotNull()
                validate(LackBetsCondition::lifetime).isFalse()
            } else {
                validate(LackBetsCondition::lifetime).isTrue()
            }
        }
    }

    override fun mutateQuery(query: Query): Query {
        val dateRange = period?.toDateTimeRange(LocalDateTime.now(), includeCurrentPeriod!!)
        val filteredBets = query(dateRange).alias("filtered_bets")

        return query
            .addJoin {
                join(
                    filteredBets,
                    JoinType.LEFT,
                    filteredBets[CasinoBetResultStatisticTable.user],
                    PlayerTable.id
                )
            }
            .apply {
                andWhere { filteredBets[CasinoBetResultStatisticTable.user].isNull() }
                dateRange?.to?.let { andWhere { PlayerTable.createdAt lessEq it } }
            }
    }

    private fun query(dateRange: Range<LocalDateTime>?): Query {
        return CasinoBetResultStatisticTable
            .slice(CasinoBetResultStatisticTable.user)
            .selectAll()
            .apply {
                dateRange?.from?.let { andWhere { CasinoBetResultStatisticTable.createdAt greaterEq it } }
                dateRange?.to?.let { andWhere { CasinoBetResultStatisticTable.createdAt lessEq it } }
            }
    }
}
