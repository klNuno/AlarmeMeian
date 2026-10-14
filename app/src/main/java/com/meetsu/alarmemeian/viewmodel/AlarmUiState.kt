package com.meetsu.alarmemeian.viewmodel

import com.meetsu.alarmemeian.data.model.AlarmConfig
import com.meetsu.alarmemeian.sms.protocol.AlarmAction

data class AlarmUiState(
    val config: AlarmConfig = AlarmConfig(),
    val isSending: Boolean = false,
    val pendingAction: AlarmAction? = null,
    val lastResponse: String? = null,
    val error: String? = null,
    val confirmAction: AlarmAction? = null,
    val settingsSaved: Boolean = false,
    val needsReadSmsPermission: Boolean = false
)
