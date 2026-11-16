package com.chipsandmayo.schengentracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chipsandmayo.schengentracker.SchengenRules
import com.chipsandmayo.schengentracker.Trip
import com.chipsandmayo.schengentracker.TripStore
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private fun LocalDate.pretty(): String = format(dateFormat)
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
private fun days(n: Int) = if (n == 1) "1 day" else "$n days"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchengenApp(store: TripStore) {
    val trips = remember { mutableStateListOf<Trip>().apply { addAll(store.load()) } }
    val today = remember { LocalDate.now() }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Schengen 90/180 Tracker") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SummaryCard(trips, today)

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Calendar") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My trips (${trips.size})") })
            }

            when (tab) {
                0 -> CalendarTab(onAdd = { start, end ->
                    val trip = Trip.create(start, end)
                    trips.add(trip)
                    store.save(trips)
                    val firstOver = SchengenRules.overLimitDays(trips).filter { it in trip }.minOrNull()
                    val message = if (firstOver != null) {
                        "Trip added, but it takes you over 90 days from ${firstOver.pretty()}"
                    } else {
                        "Trip added (${days(trip.length)})"
                    }
                    scope.launch { snackbar.showSnackbar(message) }
                })

                else -> TripsTab(trips, today, onRemove = { trip ->
                    trips.remove(trip)
                    store.save(trips)
                    scope.launch { snackbar.showSnackbar("Trip removed") }
                })
            }
        }
    }
}

@Composable
private fun SummaryCard(trips: List<Trip>, today: LocalDate) {
    val used = SchengenRules.daysUsedOn(today, trips)
    val left = SchengenRules.MAX_DAYS - used
    val leftColor = when {
        left < 0 -> MaterialTheme.colorScheme.error
        left <= 14 -> WarnOrange
        else -> OkGreen
    }

    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Stat("$used", "days used", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            Stat("${maxOf(left, 0)}", "days left today", leftColor, Modifier.weight(1f))
        }
        Text(
            text = if (left < 0) {
                "You are ${days(-left)} over the limit."
            } else {
                "Counting days from ${SchengenRules.windowStart(today).pretty()} to today."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (left < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun Stat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTab(onAdd: (LocalDate, LocalDate) -> Unit) {
    val state = rememberDateRangePickerState()
    val start = state.selectedStartDateMillis?.toLocalDate()
    val end = state.selectedEndDateMillis?.toLocalDate()

    Column(Modifier.fillMaxSize()) {
        // Scroll up for earlier months to add past visits. The pencil icon switches to typing dates.
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
            title = {
                Text(
                    "Select the day you entered and the day you left",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                )
            },
            showModeToggle = true,
        )

        Surface(tonalElevation = 3.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    when {
                        start == null -> "Tap the day you entered the Schengen area."
                        end == null -> "Entry: ${start.pretty()}. Now tap the day you left, or add it as a 1-day visit."
                        else -> "${start.pretty()} → ${end.pretty()} (${days(Trip.create(start, end).length)})"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.padding(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { state.setSelection(null, null) },
                        enabled = start != null,
                        modifier = Modifier.weight(1f),
                    ) { Text("Clear") }
                    Button(
                        onClick = {
                            if (start != null) {
                                onAdd(start, end ?: start)
                                state.setSelection(null, null)
                            }
                        },
                        enabled = start != null,
                        modifier = Modifier.weight(1f),
                    ) { Text("Add trip") }
                }
            }
        }
    }
}

@Composable
private fun TripsTab(trips: List<Trip>, today: LocalDate, onRemove: (Trip) -> Unit) {
    if (trips.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "No trips yet. Add one from the Calendar tab. Scroll back to earlier months to add past visits.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val overLimit = SchengenRules.overLimitDays(trips)
    var toRemove by remember { mutableStateOf<Trip?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(trips.sortedByDescending { it.start }, key = { it.id }) { trip ->
            TripCard(
                trip = trip,
                countingNow = SchengenRules.daysCountingNow(trip, today),
                planned = trip.start.isAfter(today),
                overLimit = overLimit.any { it in trip },
                onRemove = { toRemove = trip },
            )
        }
    }

    toRemove?.let { trip ->
        AlertDialog(
            onDismissRequest = { toRemove = null },
            title = { Text("Remove trip?") },
            text = { Text("${trip.start.pretty()} → ${trip.end.pretty()} (${days(trip.length)})") },
            confirmButton = {
                TextButton(onClick = { onRemove(trip); toRemove = null }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { toRemove = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TripCard(trip: Trip, countingNow: Int, planned: Boolean, overLimit: Boolean, onRemove: () -> Unit) {
    Card(
        colors = if (overLimit) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${trip.start.pretty()} → ${trip.end.pretty()}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                val details = buildList {
                    add(days(trip.length))
                    when {
                        planned -> add("planned")
                        countingNow > 0 -> add("${days(countingNow)} counting now")
                        else -> add("no longer counts")
                    }
                    if (overLimit) add("over the 90-day limit")
                }
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove trip", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
