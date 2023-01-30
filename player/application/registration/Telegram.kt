package io.betforge.player.application.registration

import dev.tmsoft.lib.serialization.UUIDSerializer
import io.betforge.identity.model.identity.UserId
import io.betforge.infrastructure.domain.Currency
import kotlinx.serialization.Serializable

@Serializable
data class Telegram(
    @Serializable(with = UUIDSerializer::class)
    val userId: UserId,
    val firstName: String,
    val lastName: String,
    val currency: Currency
)
