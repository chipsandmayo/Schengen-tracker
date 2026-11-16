package com.chipsandmayo.schengentracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chipsandmayo.schengentracker.ui.SchengenApp
import com.chipsandmayo.schengentracker.ui.SchengenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = TripStore(applicationContext)
        setContent {
            SchengenTheme {
                SchengenApp(store)
            }
        }
    }
}
