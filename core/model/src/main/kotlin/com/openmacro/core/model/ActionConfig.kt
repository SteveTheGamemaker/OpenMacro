package com.openmacro.core.model

data class ActionConfig(
    val id: Long = 0,
    val macroId: Long,
    val actionBlockId: Long? = null,
    val typeId: String,
    val configJson: String = "{}",
    val sortOrder: Int = 0,
    val isEnabled: Boolean = true,
    val parentActionId: Long? = null,
) {
    val type: ActionType? get() = ActionType.fromTypeId(typeId)
}
