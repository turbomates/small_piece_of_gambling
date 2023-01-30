package io.betforge.player.model

import dev.tmsoft.lib.exposed.dao.Column
import dev.tmsoft.lib.exposed.dao.EmbeddableColumn
import dev.tmsoft.lib.exposed.dao.Embedded
import dev.tmsoft.lib.exposed.dao.EmbeddedTable
import io.betforge.player.model.details.ContactDetails
import io.betforge.player.model.details.Location
import io.betforge.player.model.details.PersonDetails
import io.betforge.player.model.details.contactDetails
import io.betforge.player.model.details.location
import io.betforge.player.model.details.personDetails
import org.jetbrains.exposed.sql.Table

class Details(
    contactDetails: ContactDetails? = null,
    location: Location? = null,
    personDetails: PersonDetails? = null
) : Embedded() {
    var contactDetails by DetailsColumn.contactDetails
    var location by DetailsColumn.location
    var personDetails by DetailsColumn.personDetails

    init {
        if (contactDetails != null) {
            this.contactDetails = contactDetails
        }
        if (location != null) {
            this.location = location
        }
        if (personDetails != null) {
            this.personDetails = personDetails
        }
    }
}

class DetailsColumn(table: Table, prefix: String = "") : EmbeddableColumn<Details>(table, prefix) {
    val contactDetails = column(DetailsColumn.contactDetails)
    val location = column(DetailsColumn.location)
    val personDetails = column(DetailsColumn.personDetails)

    override fun instance(parts: Map<Column<*>, Any?>): Details {
        return Details(
            parts[DetailsColumn.contactDetails] as ContactDetails,
            parts[DetailsColumn.location] as Location,
            parts[DetailsColumn.personDetails] as PersonDetails
        )
    }

    companion object : EmbeddedTable() {
        val contactDetails = compositeColumn { prefix -> contactDetails(prefix) }
        val location = compositeColumn { prefix -> location(prefix) }
        val personDetails = compositeColumn { prefix -> personDetails(prefix) }
    }
}

fun Table.details(prefix: String = ""): DetailsColumn {
    return DetailsColumn(this, prefix)
}
