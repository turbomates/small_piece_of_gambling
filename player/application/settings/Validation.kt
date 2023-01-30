package io.betforge.player.application.settings

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.upload.File
import dev.tmsoft.lib.validation.Error
import io.betforge.infrastructure.extensions.validation.pictureFormat
import io.betforge.infrastructure.extensions.validation.pictureSize
import io.betforge.player.application.isGender
import io.betforge.player.application.settings.command.EditDetails
import io.betforge.player.application.settings.command.UpdateAvatar
import org.valiktor.functions.isNotNull
import org.valiktor.functions.validate

class Validation {
    suspend fun onEditDetails(command: EditDetails): List<Error> {
        return validate(command) {
            validate(EditDetails::gender).isGender()
        }
    }

    suspend fun onUpdateAvatar(dto: UpdateAvatar): List<Error> {
        return validate(dto) {
            validate(UpdateAvatar::avatar).validate {
                validate(File::content).pictureSize(max = 5000)
                validate(File::extension).pictureFormat()
            }.isNotNull()
        }
    }
}
