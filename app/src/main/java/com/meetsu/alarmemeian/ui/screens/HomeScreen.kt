package com.meetsu.alarmemeian.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meetsu.alarmemeian.sms.protocol.AlarmAction
import com.meetsu.alarmemeian.ui.components.AlarmActionCard
import com.meetsu.alarmemeian.ui.components.AppToast
import com.meetsu.alarmemeian.ui.components.ConfirmationDialog
import com.meetsu.alarmemeian.ui.components.StatusBanner
import com.meetsu.alarmemeian.ui.theme.*
import com.meetsu.alarmemeian.viewmodel.AlarmViewModel

private enum class Screen { Home, Settings, About }

private data class ToastData(
    val message: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AlarmViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var toast by remember { mutableStateOf<ToastData?>(null) }
    val themeState = LocalThemeState.current
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeState.mode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    // READ_SMS permission request
    val readSmsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onReadSmsPermissionResult(granted)
    }

    LaunchedEffect(uiState.needsReadSmsPermission) {
        if (uiState.needsReadSmsPermission) {
            readSmsLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    // Error toast colors
    val errorContainer = if (isDark) Color(0xFF4A1515) else Color(0xFFFFEBEE)
    val errorContent = if (isDark) Color(0xFFFFCDD2) else Color(0xFFC62828)
    // Success toast colors
    val successContainer = if (isDark) Color(0xFF1B3A1B) else Color(0xFFE8F5E9)
    val successContent = if (isDark) Color(0xFFC8E6C9) else Color(0xFF2E7D32)

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val message = when (error) {
                "no_alarm" -> "Aucune alarme configurée"
                "timeout" -> "Pas de réponse de l'alarme"
                "send_failed" -> "Échec de l'envoi du SMS"
                "read_sms_denied" -> "Permission SMS requise pour lire la réponse"
                else -> error
            }
            toast = ToastData(message, Icons.Rounded.ErrorOutline, errorContainer, errorContent)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.settingsSaved) {
        if (uiState.settingsSaved) {
            toast = ToastData("Paramètres enregistrés", Icons.Rounded.CheckCircle, successContainer, successContent)
            viewModel.clearSettingsSaved()
        }
    }

    uiState.confirmAction?.let { action ->
        ConfirmationDialog(
            action = action,
            onConfirm = { viewModel.confirmAction() },
            onDismiss = { viewModel.dismissConfirmation() }
        )
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.Home) {
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            } else {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            }
        },
        label = "screen"
    ) { screen ->
        when (screen) {
            Screen.Settings -> SettingsScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.Home }
            )
            Screen.About -> AboutScreen(
                onBack = { currentScreen = Screen.Home }
            )
            Screen.Home -> Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Alarme Meian",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        navigationIcon = {
                            IconButton(onClick = { currentScreen = Screen.About }) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "À propos",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { themeState.cycle() }) {
                                Icon(
                                    when (themeState.mode) {
                                        1 -> Icons.Rounded.LightMode
                                        2 -> Icons.Rounded.DarkMode
                                        else -> Icons.Rounded.BrightnessAuto
                                    },
                                    contentDescription = "Changer le thème",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { currentScreen = Screen.Settings }) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = "Paramètres",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        StatusBanner(
                            message = uiState.lastResponse?.let { "Réponse : $it" },
                            onDismiss = { viewModel.clearLastResponse() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .padding(bottom = 20.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.widthIn(max = 400.dp),
                            contentPadding = PaddingValues(0.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                AlarmActionCard(
                                    label = "Armer",
                                    icon = Icons.Default.Lock,
                                    containerColor = if (isDark) ArmRedContainerDark else ArmRedContainer,
                                    contentColor = if (isDark) ArmRedOnContainerDark else ArmRed,
                                    isLoading = uiState.pendingAction == AlarmAction.ARM,
                                    enabled = !uiState.isSending,
                                    onClick = { viewModel.requestAction(AlarmAction.ARM) }
                                )
                            }
                            item {
                                AlarmActionCard(
                                    label = "Armer\npartiel",
                                    icon = Icons.Default.Security,
                                    containerColor = if (isDark) PartialOrangeContainerDark else PartialOrangeContainer,
                                    contentColor = if (isDark) PartialOrangeOnContainerDark else PartialOrange,
                                    isLoading = uiState.pendingAction == AlarmAction.PARTIAL_ARM,
                                    enabled = !uiState.isSending,
                                    onClick = { viewModel.requestAction(AlarmAction.PARTIAL_ARM) }
                                )
                            }
                            item {
                                AlarmActionCard(
                                    label = "Désarmer",
                                    icon = Icons.Default.LockOpen,
                                    containerColor = if (isDark) DisarmGreenContainerDark else DisarmGreenContainer,
                                    contentColor = if (isDark) DisarmGreenOnContainerDark else DisarmGreen,
                                    isLoading = uiState.pendingAction == AlarmAction.DISARM,
                                    enabled = !uiState.isSending,
                                    onClick = { viewModel.requestAction(AlarmAction.DISARM) }
                                )
                            }
                            item {
                                AlarmActionCard(
                                    label = "Statut",
                                    icon = Icons.Default.Info,
                                    containerColor = if (isDark) StatusBlueContainerDark else StatusBlueContainer,
                                    contentColor = if (isDark) StatusBlueOnContainerDark else StatusBlue,
                                    isLoading = uiState.pendingAction == AlarmAction.STATUS,
                                    enabled = !uiState.isSending,
                                    onClick = { viewModel.requestAction(AlarmAction.STATUS) }
                                )
                            }
                        }
                    }

                    // Toast overlay at the top
                    AppToast(
                        message = toast?.message,
                        icon = toast?.icon ?: Icons.Rounded.ErrorOutline,
                        containerColor = toast?.containerColor ?: errorContainer,
                        contentColor = toast?.contentColor ?: errorContent,
                        onDismiss = { toast = null },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
