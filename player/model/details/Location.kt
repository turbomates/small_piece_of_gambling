package io.betforge.player.model.details

import dev.tmsoft.lib.exposed.dao.Column
import dev.tmsoft.lib.exposed.dao.EmbeddableColumn
import dev.tmsoft.lib.exposed.dao.Embedded
import dev.tmsoft.lib.exposed.dao.EmbeddedTable
import org.jetbrains.exposed.sql.Table

class Location(
    country: String?,
    zip: String?,
    state: String?,
    city: String?,
    street: String?,
    house: String?
) : Embedded() {
    var country by LocationColumn.country
    var zip by LocationColumn.zip
    var state by LocationColumn.state
    var city by LocationColumn.city
    var street by LocationColumn.street
    var house by LocationColumn.house

    init {
        this.country = country
        this.zip = zip
        this.state = state
        this.city = city
        this.street = street
        this.house = house
    }
}

class LocationColumn(table: Table, prefix: String = "") : EmbeddableColumn<Location>(table, prefix) {
    val country = column(LocationColumn.country)
    val zip = column(LocationColumn.zip)
    val state = column(LocationColumn.state)
    val city = column(LocationColumn.city)
    val street = column(LocationColumn.street)
    val house = column(LocationColumn.house)

    override fun instance(parts: Map<Column<*>, Any?>): Location {
        return Location(
            parts[LocationColumn.country] as? String,
            parts[LocationColumn.zip] as? String,
            parts[LocationColumn.state] as? String,
            parts[LocationColumn.city] as? String,
            parts[LocationColumn.street] as? String,
            parts[LocationColumn.house] as? String
        )
    }

    companion object : EmbeddedTable() {
        val country = column { prefix -> varchar(prefix + "country", 255).nullable() }
        val zip = column { prefix -> varchar(prefix + "zip", 255).nullable() }
        val state = column { prefix -> varchar(prefix + "state", 255).nullable() }
        val city = column { prefix -> varchar(prefix + "city", 255).nullable() }
        val street = column { prefix -> varchar(prefix + "street", 255).nullable() }
        val house = column { prefix -> varchar(prefix + "house", 255).nullable() }
    }
}

fun Table.location(prefix: String = ""): LocationColumn {
    return LocationColumn(this, prefix)
}
