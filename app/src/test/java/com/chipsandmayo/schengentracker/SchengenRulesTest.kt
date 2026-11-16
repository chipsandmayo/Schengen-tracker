package com.chipsandmayo.schengentracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SchengenRulesTest {

    private fun d(s: String) = LocalDate.parse(s)
    private fun trip(start: String, end: String) = Trip.create(d(start), d(end))

    @Test
    fun entryAndExitDaysBothCount() {
        assertEquals(10, trip("2026-01-01", "2026-01-10").length)
        assertEquals(1, trip("2026-01-01", "2026-01-01").length)
    }

    @Test
    fun reversedDatesAreSwapped() {
        val t = trip("2026-03-10", "2026-03-01")
        assertEquals(d("2026-03-01"), t.start)
        assertEquals(d("2026-03-10"), t.end)
    }

    @Test
    fun overlappingTripsCountOnce() {
        val trips = listOf(trip("2026-06-01", "2026-06-30"), trip("2026-06-20", "2026-07-05"))
        assertEquals(35, SchengenRules.daysUsedOn(d("2026-07-05"), trips))
    }

    @Test
    fun onlyTheLast180DaysCount() {
        val trips = listOf(
            trip("2026-01-01", "2026-01-10"),
            trip("2026-06-01", "2026-06-30"),
            trip("2026-06-20", "2026-07-05"),
        )
        // The window ending 16 Nov starts 21 May, so the January trip no longer counts.
        assertEquals(35, SchengenRules.daysUsedOn(d("2026-11-16"), trips))
        // The window ending 5 Jul starts 7 Jan, so 7-10 Jan (4 days) still count.
        assertEquals(39, SchengenRules.daysUsedOn(d("2026-07-05"), trips))
        assertEquals(55, SchengenRules.daysLeftOn(d("2026-11-16"), trips))
    }

    @Test
    fun overLimitDaysAreDetected() {
        val trips = listOf(trip("2026-01-01", "2026-04-05")) // 95 days
        val over = SchengenRules.overLimitDays(trips)
        assertEquals(5, over.size)
        assertTrue(d("2026-04-01") in over)
        assertTrue(d("2026-03-31") !in over)
    }

    @Test
    fun daysCountingNowOnlyIncludesTheCurrentWindow() {
        val t = trip("2026-05-15", "2026-05-25")
        // Window ending 16 Nov 2026 starts 21 May 2026, so 21-25 May count.
        assertEquals(5, SchengenRules.daysCountingNow(t, d("2026-11-16")))
    }
}
