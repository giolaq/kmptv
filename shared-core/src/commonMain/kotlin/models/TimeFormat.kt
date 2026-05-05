package com.kmptv.shared_core.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Multiplatform-safe formatters for epoch-millis values.
 *
 * Replaces `java.text.SimpleDateFormat` / `java.util.Date` which are JVM-only
 * and prevented `commonMain` from compiling for iOS targets.
 */
internal object TimeFormat {
    /** Formats a wall-clock timestamp as `yyyy-MM-dd` in the system time zone. */
    fun formatDate(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
        val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone).date
        return "${date.year.toString().padStart(4, '0')}-" +
            "${date.monthNumber.toString().padStart(2, '0')}-" +
            date.dayOfMonth.toString().padStart(2, '0')
    }

    /** Formats a wall-clock timestamp as a human readable string (`yyyy-MM-dd HH:mm:ss`). */
    fun formatDateTime(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
        val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
        return formatDate(epochMillis, zone) + " " +
            dt.hour.toString().padStart(2, '0') + ":" +
            dt.minute.toString().padStart(2, '0') + ":" +
            dt.second.toString().padStart(2, '0')
    }
}
