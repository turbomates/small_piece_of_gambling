package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.query.filter.addJoin
import dev.tmsoft.lib.validation.Error
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.valiktor.functions.isNotBlank

@Serializable
@SerialName("email_condition")
class EmailCondition(
    val inclusion: Inclusion,
    val pattern: String
) : MutableCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(EmailCondition::pattern).isNotBlank()
        }
    }

    override fun mutateQuery(query: Query): Query {
        val filteredEmails = query().alias("filtered_emails")
        return query.addJoin {
            join(
                filteredEmails,
                JoinType.INNER,
                filteredEmails[UsernamePasswordCredentialsTable.id],
                PlayerTable.id
            )
        }
    }

    private fun query(): Query {
        return UsernamePasswordCredentialsTable
            .slice(UsernamePasswordCredentialsTable.id)
            .selectAll()
            .filterEmails()
    }

    private fun Query.filterEmails(): Query {
        val stencil = "%${pattern.trim()}%"
        return when (inclusion) {
            Inclusion.EXCLUDE -> andWhere { UsernamePasswordCredentialsTable.email.address notLike stencil }
            Inclusion.INCLUDE -> andWhere { UsernamePasswordCredentialsTable.email.address like stencil }
        }
    }

    @Serializable
    enum class Inclusion {
        EXCLUDE,
        INCLUDE
    }
}
