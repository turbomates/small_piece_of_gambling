@file:UseSerializers(FilterMoneySerializer::class)

package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.domain.Money
import io.betforge.infrastructure.extensions.validation.isValidMoneyRange
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.infrasturcture.serializer.FilterMoneySerializer
import io.betforge.wallet.model.account.AccountTable
import io.betforge.wallet.model.account.BalanceChangesTable
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.IsNotNullOp
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.valiktor.functions.isNotEmpty
import java.util.UUID
import dev.tmsoft.lib.ktor.validate as ktorValidate

@Serializable
@SerialName("balance_condition")
class BalanceCondition constructor(val matching: Matching, val balances: List<@Contextual Range<Money>>) : StrictCondition {
    override suspend fun validate(): List<Error> {
        return ktorValidate(this) {
            validate(BalanceCondition::balances).isNotEmpty().isValidMoneyRange()
        }
    }

    override fun strictIds(): Set<UUID> {
        return balances.foldIndexed(setOf()) { index, ids: Set<UUID>, range ->
            val queryIds = query(range).map { it[AccountTable.owner]!! }.toSet()
            when (matching) {
                Matching.ALL -> if (index == 0 && ids.isEmpty()) queryIds else ids.intersect(queryIds)
                Matching.ANY -> ids + queryIds
            }
        }
    }

    private fun query(range: Range<Money>): Query {
        val currency = range.from?.currency.run { this ?: range.to?.currency }
        return BalanceChangesTable
            .join(AccountTable, JoinType.INNER, BalanceChangesTable.account, AccountTable.id)
            .slice(AccountTable.owner)
            .selectAll()
            .andWhere { IsNotNullOp(AccountTable.owner) }
            .andWhere { BalanceChangesTable.amount.currency eq currency!! }
            .groupBy(AccountTable.owner, BalanceChangesTable.amount.currency)
            .filterBalance(range)
    }

    private fun Query.filterBalance(range: Range<Money>): Query {
        return when {
            range.from != null && range.to != null -> having {
                BalanceChangesTable.amount.amount.sum() greaterEq range.from.amount and
                    (BalanceChangesTable.amount.amount.sum() lessEq range.to.amount)
            }
            range.from != null -> having { BalanceChangesTable.amount.amount.sum() greaterEq range.from.amount }
            range.to != null -> having { BalanceChangesTable.amount.amount.sum() lessEq range.to.amount }
            else -> throw UnsupportedOperationException("Cannot filter balance with empty range")
        }
    }

    @Serializable
    enum class Matching {
        ALL, ANY
    }
}
