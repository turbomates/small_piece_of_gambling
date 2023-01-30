package io.betforge.player.application.limits.admin.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.infrastructure.domain.Money
import io.betforge.player.infrasturcture.limits.RelationType
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AddMaxBetLimit(
    val amount: Money,
    val relationType: RelationType,
    @Serializable(UUIDSerializer::class)
    var relationId: UUID? = null
)
