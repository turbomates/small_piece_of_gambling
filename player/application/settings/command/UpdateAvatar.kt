@file:UseSerializers(FileSerializer::class)

package io.betforge.player.application.settings.command

import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.upload.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class UpdateAvatar(val avatar: File) {
    @Transient
    lateinit var playerId: UUID
}
