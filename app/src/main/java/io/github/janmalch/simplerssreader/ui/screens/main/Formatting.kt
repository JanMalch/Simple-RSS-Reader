package io.github.janmalch.simplerssreader.ui.screens.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.i18n.DateTimeFormatter
import androidx.core.i18n.DateTimeFormatterJdkStyleOptions
import androidx.core.i18n.DateTimeFormatterSkeletonOptions
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun rememberMainScreenDateFormatter(): MainScreenDateFormatter {
    val context = LocalContext.current
    return remember { MainScreenDateFormatter(context) }
}

class MainScreenDateFormatter(
    private val context: Context,
    private val clock: Clock = Clock.System,
    private val tz: TimeZone = TimeZone.currentSystemDefault(),
) {

    private val dtfDate = DateTimeFormatter(
        context = context,
        options = DateTimeFormatterSkeletonOptions.Builder()
            .setMonth(DateTimeFormatterSkeletonOptions.Month.ABBREVIATED)
            .setDay(DateTimeFormatterSkeletonOptions.Day.NUMERIC)
            .build()
    )
    private val dtfDateYear = DateTimeFormatter(
        context = context,
        options = DateTimeFormatterSkeletonOptions.Builder()
            .setYear(DateTimeFormatterSkeletonOptions.Year.NUMERIC)
            .setMonth(DateTimeFormatterSkeletonOptions.Month.ABBREVIATED)
            .setDay(DateTimeFormatterSkeletonOptions.Day.NUMERIC)
            .build()
    )

    private val dtfTime = DateTimeFormatter(
        options = DateTimeFormatterJdkStyleOptions.createTimeInstance(
            DateFormat.SHORT,
        )
    )

    fun format(instant: Instant): String {
        val now = clock.now()
        val today = now.toLocalDateTime(tz).date
        val subject = instant.toLocalDateTime(tz).date
        return when {
            today == subject -> dtfTime.format(instant.toEpochMilliseconds())
            today.year != subject.year -> dtfDateYear.format(instant.toEpochMilliseconds())
            else -> dtfDate.format(instant.toEpochMilliseconds())
        }
    }

}
