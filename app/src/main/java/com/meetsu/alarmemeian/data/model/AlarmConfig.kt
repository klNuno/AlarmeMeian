package com.meetsu.alarmemeian.data.model

data class AlarmConfig(
    val phoneNumber: String = "",
    val code: String = "",
    val brand: AlarmBrand = AlarmBrand.ORION_MEIAN
) {
    val isConfigured: Boolean get() = phoneNumber.isNotBlank() && code.isNotBlank()
}
