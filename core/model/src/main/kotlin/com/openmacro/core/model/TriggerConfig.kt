package com.openmacro.core.model

data class TriggerConfig(
    val id: Long = 0,
    val macroId: Long,
    val typeId: String,
    val configJson: String = "{}",
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
) {
    val type: TriggerType? get() = TriggerType.fromTypeId(typeId)
}
