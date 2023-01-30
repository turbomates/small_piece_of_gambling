package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.time.Period
import io.betforge.infrastructure.extensions.validation.isValidPeriodRange
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import java.time.LocalDateTime

@Serializable
@SerialName("period_registration_condition")
class PeriodRegistrationCondition(
    @Contextual val rangePeriod: Range<Period>
) : MutableCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(PeriodRegistrationCondition::rangePeriod).isValidPeriodRange()
        }
    }

    override fun mutateQuery(query: Query): Query {
        return query.filterRegistration()
    }

    private fun Query.filterRegistration(): Query {
        return apply {
            val dateFrom = rangePeriod.to?.run { toDateTimeRange(LocalDateTime.now()).from }
            val dateTo = rangePeriod.from?.run { toDateTimeRange(LocalDateTime.now()).from }

            dateFrom?.let { andWhere { PlayerTable.createdAt greaterEq it } }
            dateTo?.let { andWhere { PlayerTable.createdAt lessEq it } }

            if (dateFrom == null && dateTo == null) throw UnsupportedOperationException("Cannot filter period with empty range")
        }
    }
}
