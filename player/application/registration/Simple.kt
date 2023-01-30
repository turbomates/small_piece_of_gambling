package io.betforge.player.application.registration

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Simple(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID
)
