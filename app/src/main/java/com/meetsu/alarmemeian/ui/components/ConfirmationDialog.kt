package com.meetsu.alarmemeian.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.meetsu.alarmemeian.sms.protocol.AlarmAction

@Composable
fun ConfirmationDialog(
    action: AlarmAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = when (action) {
        AlarmAction.ARM -> "Voulez-vous vraiment armer l'alarme ?"
        AlarmAction.PARTIAL_ARM -> "Voulez-vous vraiment armer partiellement l'alarme ?"
        AlarmAction.DISARM -> "Voulez-vous vraiment désarmer l'alarme ?"
        AlarmAction.STATUS -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmation") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Oui")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Non")
            }
        }
    )
}
