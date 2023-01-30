package io.betforge.player.application.settings.command

import dev.tmsoft.lib.date.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate
import java.util.UUID

@Serializable
data class EditDetails(
    val firstName: String?,
    val lastName: String?,
    val zip: String?,
    val country: String?,
    val state: String?,
    val city: String?,
    val street: String?,
    val house: String?,
    val phone: String?,
    val mobile: String?,
    val gender: String?,
    @Serializable(with = LocalDateSerializer::class)
    val birthday: LocalDate?
) {
    @Transient
    lateinit var playerId: UUID
}
