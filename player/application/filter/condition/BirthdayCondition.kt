@file:UseSerializers(LocalDateSerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.date.LocalDateSerializer
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.infrastructure.extensions.time.PeriodType
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.functions.isNotNull
import java.time.LocalDate

@Serializable
@SerialName("birthday_condition")
class BirthdayCondition(
    val date: @Contextual Range<LocalDate>? = null,
    val period: Period? = null
) : MutableCondition {

    private object IncorrectPeriodType : Constraint
    private object BothBirthdayFiltersNotNull : Constraint

    override suspend fun validate(): List<Error> {
        return validate(this) {
            when {
                period == null -> validate(BirthdayCondition::date).isNotNull()
                date == null -> validate(BirthdayCondition::period).isNotNull().validate(IncorrectPeriodType) { period ->
                    period != null &&
                        period.periodType != PeriodType.YEARS &&
                        period.periodType != PeriodType.HOURS
                }
                else -> validate(BirthdayCondition::date).addConstraintViolations(listOf(object : ConstraintViolation {
                    override val constraint: Constraint = BothBirthdayFiltersNotNull
                    override val property: String = "date and period"
                    override val value: Any? = null
                }))
            }
        }
    }

    override fun mutateQuery(query: Query): Query {
        val dateRange = period?.toDateRange() ?: date
        return query
            .apply {
                dateRange?.from?.let { query.andWhere { PlayerTable.details.personDetails.birthday greaterEq it } }
                dateRange?.to?.let { query.andWhere { PlayerTable.details.personDetails.birthday lessEq it } }
            }
    }
}
