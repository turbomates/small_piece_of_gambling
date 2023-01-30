@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.group.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class CreateGroup(
    val name: String,
    val filterId: UUID? = null,
    val playerIds: List<UUID>? = null,
    val color: String,
    val priority: Int
)
