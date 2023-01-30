package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.or
import org.valiktor.functions.isNotEmpty

@Serializable
@SerialName("country_condition")
class CountryCondition(
    val inclusion: Inclusion,
    val countries: List<String>
) : MutableCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(CountryCondition::countries).isNotEmpty()
        }
    }

    override fun mutateQuery(query: Query): Query {
        return query.filterCountries()
    }

    private fun Query.filterCountries(): Query {
        return when (inclusion) {
            Inclusion.EXCLUDE -> andWhere {
                PlayerTable.details.location.country notInList countries or
                    (PlayerTable.details.location.country eq null)
            }
            Inclusion.INCLUDE -> andWhere {
                PlayerTable.details.location.country inList countries
            }
        }
    }

    @Serializable
    enum class Inclusion {
        EXCLUDE,
        INCLUDE
    }
}
