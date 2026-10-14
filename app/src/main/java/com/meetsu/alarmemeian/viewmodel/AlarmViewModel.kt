package com.meetsu.alarmemeian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meetsu.alarmemeian.data.AlarmRepository
import com.meetsu.alarmemeian.data.model.AlarmConfig
import com.meetsu.alarmemeian.sms.SmsService
import com.meetsu.alarmemeian.sms.protocol.AlarmAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)
    val smsService = SmsService(application)

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private var timeoutJob: Job? = null

    init {
        viewModelScope.launch {
            repository.config.collect { config ->
                _uiState.update { it.copy(config = config) }
            }
        }
    }

    fun requestAction(action: AlarmAction) {
        if (_uiState.value.isSending) return

        // Check READ_SMS permission first (needed to read alarm response)
        if (!smsService.canReadSms()) {
            _uiState.update { it.copy(needsReadSmsPermission = true) }
            return
        }

        if (action == AlarmAction.STATUS) {
            executeAction(action)
        } else {
            _uiState.update { it.copy(confirmAction = action) }
        }
    }

    fun onReadSmsPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needsReadSmsPermission = false) }
        if (!granted) {
            _uiState.update { it.copy(error = "read_sms_denied") }
        }
    }

    fun confirmAction() {
        val action = _uiState.value.confirmAction ?: return
        _uiState.update { it.copy(confirmAction = null) }
        executeAction(action)
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(confirmAction = null) }
    }

    private fun executeAction(action: AlarmAction) {
        val config = _uiState.value.config
        if (!config.isConfigured) {
            _uiState.update { it.copy(error = "no_alarm") }
            return
        }

        _uiState.update {
            it.copy(
                isSending = true,
                pendingAction = action,
                lastResponse = null,
                error = null
            )
        }

        val sent = smsService.sendCommand(config, action) { response ->
            timeoutJob?.cancel()
            _uiState.update {
                it.copy(
                    isSending = false,
                    pendingAction = null,
                    lastResponse = response
                )
            }
        }

        if (!sent) {
            _uiState.update {
                it.copy(isSending = false, pendingAction = null, error = "send_failed")
            }
            return
        }

        // 60s timeout (longer because user must manually send SMS)
        timeoutJob = viewModelScope.launch {
            delay(60_000)
            smsService.unregisterAll()
            _uiState.update {
                it.copy(
                    isSending = false,
                    pendingAction = null,
                    error = "timeout"
                )
            }
        }
    }

    fun saveConfig(phoneNumber: String, code: String, brand: com.meetsu.alarmemeian.data.model.AlarmBrand) {
        viewModelScope.launch {
            repository.saveConfig(AlarmConfig(phoneNumber, code, brand))
            _uiState.update { it.copy(settingsSaved = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSettingsSaved() {
        _uiState.update { it.copy(settingsSaved = false) }
    }

    fun clearLastResponse() {
        _uiState.update { it.copy(lastResponse = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
        smsService.unregisterAll()
    }
}
