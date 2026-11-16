package com.chipsandmayo.schengentracker

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/** One visit to the Schengen area. Both the entry day and the exit day count as days spent there. */
data class Trip(val id: String, val start: LocalDate, val end: LocalDate) {

    val length: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    fun days(): Sequence<LocalDate> = generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }

    companion object {
        /** Creates a trip, swapping the dates if they were picked in the wrong order. */
        fun create(start: LocalDate, end: LocalDate): Trip {
            val (s, e) = if (end.isBefore(start)) end to start else start to end
            return Trip(UUID.randomUUID().toString(), s, e)
        }
    }
}

/**
 * The Schengen short-stay rule: at most 90 days in ANY rolling 180-day period.
 * For a given date, look back over the 180 days ending on (and including) that date
 * and count every day spent in the area.
 */
object SchengenRules {
    const val MAX_DAYS = 90
    const val WINDOW_DAYS = 180

    /** First day of the 180-day window that ends on [date]. */
    fun windowStart(date: LocalDate): LocalDate = date.minusDays(WINDOW_DAYS - 1L)

    /** Every distinct day spent in the area. Overlapping trips are only counted once. */
    fun stayDays(trips: List<Trip>): Set<LocalDate> = buildSet { trips.forEach { addAll(it.days()) } }

    fun daysUsedOn(date: LocalDate, stayDays: Set<LocalDate>): Int =
        (0 until WINDOW_DAYS).count { date.minusDays(it.toLong()) in stayDays }

    fun daysUsedOn(date: LocalDate, trips: List<Trip>): Int = daysUsedOn(date, stayDays(trips))

    /** Days still allowed on [date]. Negative means over the limit. */
    fun daysLeftOn(date: LocalDate, trips: List<Trip>): Int = MAX_DAYS - daysUsedOn(date, trips)

    /** Days spent in the area on which the 90-day limit was (or would be) exceeded. */
    fun overLimitDays(trips: List<Trip>): Set<LocalDate> {
        val stays = stayDays(trips)
        return stays.filterTo(mutableSetOf()) { daysUsedOn(it, stays) > MAX_DAYS }
    }

    /** How many of a trip's days fall inside the 180-day window ending [today]. */
    fun daysCountingNow(trip: Trip, today: LocalDate): Int {
        val from = windowStart(today)
        return trip.days().count { !it.isBefore(from) && !it.isAfter(today) }
    }
}
