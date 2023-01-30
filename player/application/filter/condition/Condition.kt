package io.betforge.player.application.filter.condition

import dev.tmsoft.lib.validation.Error
import io.betforge.player.infrasturcture.filter.Range
import io.betforge.player.infrasturcture.serializer.FilterRangeSerializer
import io.betforge.player.model.PlayerTable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import java.util.UUID

interface Condition {
    suspend fun validate(): List<Error>

    companion object {
        val module = SerializersModule {
            polymorphic(Condition::class) {
                subclass(BalanceCondition::class, BalanceCondition.serializer())
                subclass(LossSumCondition::class, LossSumCondition.serializer())
                subclass(WagerSumCondition::class, WagerSumCondition.serializer())
                subclass(GenderCondition::class, GenderCondition.serializer())
                subclass(WinSumCondition::class, WinSumCondition.serializer())
                subclass(LocaleCondition::class, LocaleCondition.serializer())
                subclass(CountryCondition::class, CountryCondition.serializer())
                subclass(RegistrationCondition::class, RegistrationCondition.serializer())
                subclass(EmailCondition::class, EmailCondition.serializer())
                subclass(BirthdayCondition::class, BirthdayCondition.serializer())
                subclass(IpCondition::class, IpCondition.serializer())
                subclass(AccessCondition::class, AccessCondition.serializer())
                subclass(PeriodRegistrationCondition::class, PeriodRegistrationCondition.serializer())
                subclass(LackBetsCondition::class, LackBetsCondition.serializer())
                subclass(WinCountCondition::class, WinCountCondition.serializer())
                subclass(WagerCountCondition::class, WagerCountCondition.serializer())
                subclass(LossCountCondition::class, LossCountCondition.serializer())
            }

            contextual(Range::class) { types ->
                FilterRangeSerializer(types.first())
            }
        }
    }
}

fun Query.applyConditions(conditions: List<Condition>): Query {
    var playerIDs: Set<UUID>? = null
    conditions.forEach { condition ->
        when (condition) {
            is MutableCondition ->
                condition.mutateQuery(this)
            is StrictCondition ->
                playerIDs = playerIDs?.intersect(condition.strictIds()) ?: condition.strictIds()
        }
    }

    return playerIDs?.let { andWhere { PlayerTable.id inList it } } ?: this
}
