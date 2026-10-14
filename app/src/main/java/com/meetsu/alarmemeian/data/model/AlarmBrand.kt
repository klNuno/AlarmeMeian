package com.meetsu.alarmemeian.data.model

enum class AlarmBrand(val displayName: String) {
    ORION_MEIAN("Orion / Meian");

    companion object {
        fun fromName(name: String): AlarmBrand =
            entries.firstOrNull { it.name == name } ?: ORION_MEIAN
    }
}
