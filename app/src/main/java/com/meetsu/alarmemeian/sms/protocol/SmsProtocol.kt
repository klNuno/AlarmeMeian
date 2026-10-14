package com.meetsu.alarmemeian.sms.protocol

interface SmsProtocol {
    fun formatCommand(code: String, action: AlarmAction): String
}

enum class AlarmAction {
    ARM,
    PARTIAL_ARM,
    DISARM,
    STATUS
}
