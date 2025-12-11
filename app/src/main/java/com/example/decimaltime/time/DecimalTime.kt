package com.example.decimaltime.time

import java.time.LocalTime
import kotlin.math.floor

data class DecimalTime(val hours: Int, val minutes: Int, val seconds: Int) {
    override fun toString(): String = "%d:%02d:%02d".format(hours, minutes, seconds)
}

object DecimalTimeFormatter {
    private const val SECONDS_IN_DAY = 86_400
    private const val DECIMAL_MAX = 100_000

    fun now(): DecimalTime = fromLocalTime(LocalTime.now())

    fun fromLocalTime(time: LocalTime): DecimalTime {
        val totalSeconds = time.toSecondOfDay()
        val decimalTotal = totalSeconds * (DECIMAL_MAX.toDouble() / SECONDS_IN_DAY.toDouble())

        val hours = floor(decimalTotal / 10_000).toInt()
        val minutes = floor((decimalTotal % 10_000) / 100).toInt()
        val seconds = floor(decimalTotal % 100).toInt()
        return DecimalTime(hours, minutes, seconds)
    }

    fun formatStandardTimeForDecimalHour(decimalHour: Int): String {
        val totalMinutes = decimalHour * 144 // 24h / 10
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return "%02d:%02d".format(hours, minutes)
    }

    fun decimalTimeline(): List<DecimalHourHint> =
        (0..10).map { hour ->
            DecimalHourHint(
                hour,
                formatStandardTimeForDecimalHour(hour)
            )
        }
}

data class DecimalHourHint(
    val decimalHour: Int,
    val standardStart: String
)
