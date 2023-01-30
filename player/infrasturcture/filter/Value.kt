package io.betforge.player.infrasturcture.filter

import dev.tmsoft.lib.query.filter.ListValue
import dev.tmsoft.lib.query.filter.MapValue
import dev.tmsoft.lib.query.filter.RangeValue
import dev.tmsoft.lib.query.filter.SingleValue
import dev.tmsoft.lib.query.filter.Value
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

fun Value.convertToJsonElement(): JsonElement {
    return when (val that = this) {
        is SingleValue -> when (that.value) {
            "true" -> JsonPrimitive(true)
            "false" -> JsonPrimitive(false)
            else -> Json.encodeToJsonElement(that.value)
        }
        is ListValue -> buildJsonArray { that.values.forEach { add(it.convertToJsonElement()) } }
        is MapValue -> buildJsonObject { that.value.map { put(it.key, it.value.convertToJsonElement()) } }
        is RangeValue -> {
            val from = that.from.orEmpty()
            val to = that.to.orEmpty()
            Json.encodeToJsonElement("$from~$to")
        }
        else -> Json.encodeToJsonElement(this)
    }
}
