package io.betforge.player.application.group.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EditGroup(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val color: String,
    val priority: Int,
    @Serializable(with = UUIDSerializer::class)
    val filterId: UUID?
)
