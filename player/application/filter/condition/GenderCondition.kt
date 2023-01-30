package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.validation.Error
import io.betforge.player.model.PlayerTable
import io.betforge.player.model.details.Gender
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere

@Serializable
@SerialName("gender_condition")
class GenderCondition(
    val gender: GenderType
) : MutableCondition {

    override suspend fun validate(): List<Error> {
        return emptyList()
    }

    override fun mutateQuery(query: Query): Query {
        return query.andWhere { PlayerTable.details.personDetails.gender eq gender.convert() }
    }

    @Serializable
    enum class GenderType {
        MALE,
        FEMALE,
        NOT_SPECIFIED;

        fun convert(): Gender? {
            return when (this) {
                MALE -> Gender.MALE
                FEMALE -> Gender.FEMALE
                NOT_SPECIFIED -> null
            }
        }
    }
}
