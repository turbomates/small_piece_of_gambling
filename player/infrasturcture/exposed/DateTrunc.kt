package io.betforge.player.infrasturcture.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.javatime.JavaLocalDateColumnType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Column<*>.dateTrunc(
    period: TruncPeriod,
    localDateTime: LocalDateTime = LocalDateTime.now()
): DateTrunc = DateTrunc(localDateTime, period)

class DateTrunc(
    private val localDateTime: LocalDateTime,
    private val truncPeriod: TruncPeriod
) : Function<LocalDate>(JavaLocalDateColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +"date_trunc("
        +"'${truncPeriod.value}',"
        +"TIMESTAMP"
        +"'${localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'"
        +")"
    }
}

enum class TruncPeriod(val value: String) {
    MONTH("month"), YEAR("year")
}
