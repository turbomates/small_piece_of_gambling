package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.identity.model.identity.LoginHistoryTable
import io.betforge.infrastructure.extensions.validation.isIP
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.valiktor.functions.isNotBlank
import java.util.UUID

@Serializable
@SerialName("ip_condition")
class IpCondition(
    val inclusion: Inclusion,
    val ip: String
) : StrictCondition {

    override suspend fun validate(): List<Error> {
        return validate(this) {
            validate(IpCondition::ip).isNotBlank().isIP()
        }
    }

    override fun strictIds(): Set<UUID> {
        return query().map { it[LoginHistoryTable.userId] }.toSet()
    }

    private fun query(): Query {
        return LoginHistoryTable.slice(LoginHistoryTable.userId)
            .selectAll()
            .filterIPs()
    }

    private fun Query.filterIPs(): Query {
        return when (inclusion) {
            Inclusion.EXCLUDE -> andWhere { LoginHistoryTable.fromIp neq ip }
            Inclusion.INCLUDE -> andWhere { LoginHistoryTable.fromIp eq ip }
        }
    }

    @Serializable
    enum class Inclusion {
        EXCLUDE,
        INCLUDE
    }
}
