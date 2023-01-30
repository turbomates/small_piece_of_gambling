package io.betforge.player.application.group.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DeleteGroup(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
