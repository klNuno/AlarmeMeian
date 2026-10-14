package com.meetsu.alarmemeian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModelProvider
import com.meetsu.alarmemeian.data.AlarmRepository
import com.meetsu.alarmemeian.ui.screens.HomeScreen
import com.meetsu.alarmemeian.ui.theme.AlarmeMeianTheme
import com.meetsu.alarmemeian.ui.theme.ThemeState
import com.meetsu.alarmemeian.viewmodel.AlarmViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AlarmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        val repository = AlarmRepository(applicationContext)
        val initialMode = runBlocking { repository.themeMode.first() }

        setContent {
            val themeState = remember { ThemeState(initialMode) }

            LaunchedEffect(Unit) {
                snapshotFlow { themeState.mode }
                    .collect { mode ->
                        repository.saveThemeMode(mode)
                    }
            }

            AlarmeMeianTheme(themeState = themeState) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::viewModel.isInitialized) {
            viewModel.smsService.unregisterAll()
        }
    }
}
