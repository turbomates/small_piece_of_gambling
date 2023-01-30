package io.betforge.player.application

import dev.tmsoft.lib.query.filter.Field
import dev.tmsoft.lib.query.filter.Filter
import dev.tmsoft.lib.query.filter.addJoin
import io.betforge.identity.model.identity.LoginHistoryTable
import io.betforge.identity.model.identity.UserTable
import io.betforge.identity.model.identity.credentials.UsernamePasswordCredentialsTable
import io.betforge.infrastructure.extensions.money
import io.betforge.player.application.filter.condition.AccessCondition
import io.betforge.player.application.filter.condition.BalanceCondition
import io.betforge.player.application.filter.condition.BirthdayCondition
import io.betforge.player.application.filter.condition.Condition
import io.betforge.player.application.filter.condition.CountryCondition
import io.betforge.player.application.filter.condition.EmailCondition
import io.betforge.player.application.filter.condition.GenderCondition
import io.betforge.player.application.filter.condition.IpCondition
import io.betforge.player.application.filter.condition.LackBetsCondition
import io.betforge.player.application.filter.condition.LocaleCondition
import io.betforge.player.application.filter.condition.LossCountCondition
import io.betforge.player.application.filter.condition.LossSumCondition
import io.betforge.player.application.filter.condition.PeriodRegistrationCondition
import io.betforge.player.application.filter.condition.RegistrationCondition
import io.betforge.player.application.filter.condition.WagerCountCondition
import io.betforge.player.application.filter.condition.WagerSumCondition
import io.betforge.player.application.filter.condition.WinCountCondition
import io.betforge.player.application.filter.condition.WinSumCondition
import io.betforge.player.application.filter.condition.applyConditions
import io.betforge.player.infrasturcture.filter.convertToJsonElement
import io.betforge.player.model.PlayerFilterTable
import io.betforge.player.model.PlayerTable
import io.betforge.player.model.group.PlayerGroupsTable
import io.betforge.wallet.model.account.AccountTable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.sql.AndOp
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.OrOp
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import io.betforge.infrastructure.json.json as defaultJson

object PlayersFilter : Filter(PlayerTable) {
    val wagerSumCondition = addCondition("wager_sum_condition", WagerSumCondition.serializer())
    val winSumCondition = addCondition("win_sum_condition", WinSumCondition.serializer())
    val lossSumCondition = addCondition("loss_sum_condition", LossSumCondition.serializer())
    val balanceCondition = addCondition("balance_condition", BalanceCondition.serializer())
    val genderCondition = addCondition("gender_condition", GenderCondition.serializer())
    val localeCondition = addCondition("locale_condition", LocaleCondition.serializer())
    val countryCondition = addCondition("country_condition", CountryCondition.serializer())
    val registrationCondition = addCondition("registration_condition", RegistrationCondition.serializer())
    val emailCondition = addCondition("email_condition", EmailCondition.serializer())
    val birthdayCondition = addCondition("birthday_condition", BirthdayCondition.serializer())
    val ipCondition = addCondition("ip_condition", IpCondition.serializer())
    val accessCondition = addCondition("access_condition", AccessCondition.serializer())
    val periodRegistrationCondition = addCondition("period_registration_condition", PeriodRegistrationCondition.serializer())
    val lackBetsCondition = addCondition("lack_bets_condition", LackBetsCondition.serializer())
    val winCountCondition = addCondition("win_count_condition", WinCountCondition.serializer())
    val wagerCountCondition = addCondition("wager_count_condition", WagerCountCondition.serializer())
    val lossCountCondition = addCondition("loss_count_condition", LossCountCondition.serializer())

    val saved = add("saved", PlayerFilterTable.name) { values ->
        val conditions = PlayerFilterTable.select(
            OrOp(values.map { it.op(PlayerFilterTable.name) })
        ).flatMap { it[PlayerFilterTable.conditions] }

        applyConditions(conditions)
    }

    val group = add("group", PlayerGroupsTable.id) { values ->
        val playerIds = PlayerGroupsTable.select(
            OrOp(values.map { it.op(PlayerGroupsTable.id) })
        ).flatMap { it[PlayerGroupsTable.playerIds].toList() }

        andWhere { PlayerTable.id inList playerIds }
    }

    val email = add("email", UsernamePasswordCredentialsTable.email.address)
    val username = add("username", UserTable.username)
    val currency = add("currency", AccountTable.balance.currency) { values ->
        andWhere {
            exists(
                AccountTable.slice(AccountTable.owner).select(
                    AndOp(
                        listOf(OrOp(values.map { it.op(AccountTable.balance.currency) }), PlayerTable.id eq AccountTable.owner)
                    )
                )
            )
        }
    }
    val balance = add("balance", AccountTable.balance.amount) { values ->
        andWhere {
            exists(AccountTable.slice(AccountTable.owner).select(AndOp(listOf(OrOp(values.map {
                it.money(AccountTable.balance.amount, AccountTable.balance.currency)
            }), PlayerTable.id eq AccountTable.owner))))
        }
    }
    val signedUp = add("created_at")
    val lastLoginIn = add("last_login_at", LoginHistoryTable.createdAt) { values ->
        val columnMaxDate = LoginHistoryTable.createdAt.max().alias("max")
        val maxDate = LoginHistoryTable.slice(columnMaxDate, LoginHistoryTable.userId)
            .selectAll()
            .groupBy(LoginHistoryTable.userId)
            .alias("login_history")
        addJoin {
            join(maxDate, JoinType.LEFT, maxDate[LoginHistoryTable.userId], UserTable.id)
        }
            .andWhere {
                OrOp(values.map { it.op(columnMaxDate.aliasOnlyExpression() as ExpressionWithColumnType<*>) })
            }
    }
    val country = add("country", PlayerTable.details.location.country)
    val firstName = add("first_name", PlayerTable.details.personDetails.name.first)
    val lastName = add("last_name", PlayerTable.details.personDetails.name.last)
    val birthday = add("birthday", PlayerTable.details.personDetails.birthday)
    val status = add("status")

    private fun <T : Condition> addCondition(name: String, serializer: KSerializer<T>): Field {
        return add(name) { values ->
            val conditions = values.map {
                val condition: Condition = defaultJson.decodeFromJsonElement(serializer, it.convertToJsonElement())
                runBlocking { if (condition.validate().isNotEmpty()) throw UnsupportedOperationException("Cannot apply condition") }
                condition
            }
            applyConditions(conditions)
        }
    }
}
