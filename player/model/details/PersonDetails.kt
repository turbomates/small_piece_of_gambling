package io.betforge.player.model.details

import dev.tmsoft.lib.exposed.dao.Column
import dev.tmsoft.lib.exposed.dao.EmbeddableColumn
import dev.tmsoft.lib.exposed.dao.Embedded
import dev.tmsoft.lib.exposed.dao.EmbeddedTable
import io.betforge.infrastructure.domain.Name
import io.betforge.infrastructure.domain.NameColumn
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate

class PersonDetails(name: Name?, birthday: LocalDate? = null, gender: Gender? = null) : Embedded() {
    var name by PersonDetailsColumn.name
        private set
    var birthday by PersonDetailsColumn.birthday
        private set
    var gender by PersonDetailsColumn.gender
        private set

    init {
        if (name != null) {
            this.name = name
        }
        this.birthday = birthday
        this.gender = gender
    }
}

class PersonDetailsColumn(table: Table, prefix: String = "") : EmbeddableColumn<PersonDetails>(table, prefix) {
    val name = column(PersonDetailsColumn.name)
    val birthday = column(PersonDetailsColumn.birthday)
    val gender = column(PersonDetailsColumn.gender)

    override fun instance(parts: Map<Column<*>, Any?>): PersonDetails {
        return PersonDetails(
            parts[PersonDetailsColumn.name] as Name,
            parts[PersonDetailsColumn.birthday] as? LocalDate,
            parts[PersonDetailsColumn.gender] as? Gender
        )
    }

    companion object : EmbeddedTable() {
        val name = compositeColumn { prefix -> NameColumn(this, prefix) }
        val birthday = column { prefix -> date(prefix + "birthday").nullable() }
        val gender = column { prefix -> enumerationByName(prefix + "gender", 25, Gender::class).nullable() }
    }
}

fun Table.personDetails(prefix: String = ""): PersonDetailsColumn {
    return PersonDetailsColumn(this, prefix)
}

@Serializable
enum class Gender {
    MALE, FEMALE
}
