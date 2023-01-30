@file:UseSerializers(LocalDateTimeSerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.date.LocalDateTimeSerializer
import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.validation.isValidRange
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import java.time.LocalDateTime

@Serializable
@SerialName("registration_condition")
class RegistrationCondition(@Contextual val date: Range<LocalDateTime>) : MutableCondition {
    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(RegistrationCondition::date).isValidRange()
        }
    }

    override fun mutateQuery(query: Query): Query {
        return query
            .apply { date.from?.let { query.andWhere { PlayerTable.createdAt greaterEq it } } }
            .apply { date.to?.let { query.andWhere { PlayerTable.createdAt lessEq it } } }
    }
}
