@file:UseSerializers(LocaleSerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.query.filter.addJoin
import dev.tmsoft.lib.serialization.LocaleSerializer
import dev.tmsoft.lib.validation.Error
import io.betforge.identity.model.identity.UserTable
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.valiktor.functions.isNotEmpty
import java.util.Locale

@Serializable
@SerialName("locale_condition")
class LocaleCondition(
    val inclusion: Inclusion,
    val locales: List<Locale>
) : MutableCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(LocaleCondition::locales).isNotEmpty()
        }
    }

    override fun mutateQuery(query: Query): Query {
        val filteredLocales = query().alias("filtered_locales")
        return query.addJoin {
            join(
                filteredLocales,
                JoinType.INNER,
                filteredLocales[UserTable.id],
                PlayerTable.id
            )
        }
    }

    private fun query(): Query {
        return UserTable.slice(UserTable.id)
            .selectAll()
            .filterLocales()
    }

    private fun Query.filterLocales(): Query {
        return when (inclusion) {
            Inclusion.EXCLUDE -> andWhere { UserTable.preferences.locale notInList locales }
            Inclusion.INCLUDE -> andWhere { UserTable.preferences.locale inList locales }
        }
    }

    @Serializable
    enum class Inclusion {
        EXCLUDE,
        INCLUDE
    }
}
