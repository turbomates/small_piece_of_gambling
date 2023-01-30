package io.betforge.player.model.details

import dev.tmsoft.lib.exposed.dao.Column
import dev.tmsoft.lib.exposed.dao.EmbeddableColumn
import dev.tmsoft.lib.exposed.dao.Embedded
import dev.tmsoft.lib.exposed.dao.EmbeddedTable
import org.jetbrains.exposed.sql.Table

class ContactDetails(mobile: String?, phone: String?) : Embedded() {
    var mobile by ContactDetailsColumn.mobile
        private set
    var phone by ContactDetailsColumn.phone
        private set

    init {
        this.mobile = mobile
        this.phone = phone
    }
}

class ContactDetailsColumn(table: Table, prefix: String = "") : EmbeddableColumn<ContactDetails>(table, prefix) {
    val mobile = column(ContactDetailsColumn.mobile)
    val phone = column(ContactDetailsColumn.phone)

    override fun instance(parts: Map<Column<*>, Any?>): ContactDetails {
        return ContactDetails(
            parts[ContactDetailsColumn.mobile] as? String,
            parts[ContactDetailsColumn.phone] as? String
        )
    }

    companion object : EmbeddedTable() {
        val mobile = column { prefix -> varchar(prefix + "mobile_number", 255).nullable() }
        val phone = column { prefix -> varchar(prefix + "phone_number", 255).nullable() }
    }
}

fun Table.contactDetails(prefix: String = ""): ContactDetailsColumn {
    return ContactDetailsColumn(this, prefix)
}
