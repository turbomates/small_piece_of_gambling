package io.betforge.player.application.registration

import dev.tmsoft.lib.date.LocalDateSerializer
import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class UK(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    @Serializable(with = LocalDateSerializer::class)
    val birthday: LocalDate,
    val gender: String,
    val zip: String,
    val country: String,
    val state: String,
    val city: String,
    val street: String,
    val house: String,
    val phone: String,
    val mobile: String
)
