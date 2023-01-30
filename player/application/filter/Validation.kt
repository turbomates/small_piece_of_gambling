package io.betforge.player.application.filter

import dev.tmsoft.lib.ktor.validate
import dev.tmsoft.lib.validation.Error
import io.betforge.player.application.filter.command.AddFilter
import io.betforge.player.application.filter.command.EditFilter
import org.valiktor.functions.isNotEmpty

class Validation {
    suspend fun onAddFilter(command: AddFilter): List<Error> {
        return validate(command) {
            validate(AddFilter::name).isNotEmpty()
            validate(AddFilter::conditions).isNotEmpty()
        } + command.conditions.flatMap { it.validate() }
    }

    suspend fun onEditFilter(command: EditFilter): List<Error> {
        return validate(command) {
            validate(EditFilter::name).isNotEmpty()
            validate(EditFilter::conditions).isNotEmpty()
        } + command.conditions.flatMap { it.validate() }
    }
}
