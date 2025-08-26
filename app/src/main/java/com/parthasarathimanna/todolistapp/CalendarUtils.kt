package com.parthasarathimanna.todolistapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object CalendarUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    var selectedDate: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    fun formattedDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return date.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formattedTime(): String {
        val formatter =DateTimeFormatter.ofPattern("hh:mm:ss a")
        return LocalTime.now().format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysInMonthArray(date: LocalDate): List<LocalDate?> {
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = (firstOfMonth.dayOfWeek.value + 6) % 7 // Adjust for 0-based Sunday
        val totalCells = 42 // Grid size: 6 rows × 7 columns

        return List(totalCells) { index ->
            when {
                index < dayOfWeek -> null
                index >= daysInMonth + dayOfWeek -> null
                else -> firstOfMonth.plusDays((index - dayOfWeek).toLong())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysInWeekArray(selectedDate: LocalDate): ArrayList<String> {
        val days = ArrayList<String>()
        var current = sundayForDate(selectedDate)
        val endDate = current.plusWeeks(1)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Adjust the format as needed

        while (current.isBefore(endDate)) {
            days.add(current.format(formatter))
            current = current.plusDays(1)
        }
        return days
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sundayForDate(current: LocalDate): LocalDate {
        var date = current
        val oneWeekAgo = current.minusWeeks(1)

        while (date.isAfter(oneWeekAgo)) {
            if (date.dayOfWeek == DayOfWeek.SUNDAY) {
                return date
            }
            date = date.minusDays(1)
        }
        return date
    }
}
