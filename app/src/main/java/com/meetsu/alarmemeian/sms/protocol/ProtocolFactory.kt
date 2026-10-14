package com.meetsu.alarmemeian.sms.protocol

import com.meetsu.alarmemeian.data.model.AlarmBrand

object ProtocolFactory {
    fun getProtocol(brand: AlarmBrand): SmsProtocol = when (brand) {
        AlarmBrand.ORION_MEIAN -> OrionProtocol()
    }
}
