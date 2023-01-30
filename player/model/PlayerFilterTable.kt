package io.betforge.player.model

import dev.tmsoft.lib.exposed.type.jsonb
import io.betforge.player.application.filter.condition.Condition
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object PlayerFilterTable : UUIDTable("player_filters") {
    val name = varchar("name", 255)
    val conditions = jsonb("conditions", ListSerializer<Condition>(Condition.module.serializer()), Condition.module).default(emptyList())
    val count = integer("count").default(0)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
