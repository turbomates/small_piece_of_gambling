package io.betforge.player.application

import org.valiktor.Constraint
import org.valiktor.Validator

object Gender : Constraint

internal fun <E> Validator<E>.Property<String?>.isGender(): Validator<E>.Property<String?> =
    this.validate(Gender) { value ->
        value == null || io.betforge.player.model.details.Gender.values().any { it.name == value }
    }
