@file:UseSerializers(UUIDSerializer::class)

package io.betforge.player.application.group.command

import dev.tmsoft.lib.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class UpdatePlayersInGroup(
    val groupId: UUID,
    val playerIds: List<UUID>
)
