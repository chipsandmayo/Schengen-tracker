package com.chipsandmayo.schengentracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Saves trips on the device as JSON in SharedPreferences. */
class TripStore(context: Context) {

    private val prefs = context.getSharedPreferences("schengen_trips", Context.MODE_PRIVATE)

    fun load(): List<Trip> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Trip(
                id = o.getString("id"),
                start = LocalDate.parse(o.getString("start")),
                end = LocalDate.parse(o.getString("end")),
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun save(trips: List<Trip>) {
        val array = JSONArray()
        trips.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("start", it.start.toString())
                    .put("end", it.end.toString())
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val KEY = "trips"
    }
}
