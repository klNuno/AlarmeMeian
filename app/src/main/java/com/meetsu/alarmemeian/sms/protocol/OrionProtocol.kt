package com.meetsu.alarmemeian.sms.protocol

class OrionProtocol : SmsProtocol {

    override fun formatCommand(code: String, action: AlarmAction): String {
        val actionStr = when (action) {
            AlarmAction.ARM -> "ARMER"
            AlarmAction.PARTIAL_ARM -> "ARMERPARTIEL"
            AlarmAction.DISARM -> "DESARMER"
            AlarmAction.STATUS -> "STATUT"
        }
        return "#ORION$code #$actionStr"
    }
}
