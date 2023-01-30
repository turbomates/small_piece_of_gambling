package io.betforge.player.application.filter.command

import java.util.UUID

data class UpdateCountFilter(
    val id: UUID,
    val count: Int
)
