package com.openmacro.core.model

data class Macro(
    val id: Long = 0,
    val name: String,
    val categoryId: Long? = null,
    val isEnabled: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class MacroWithDetails(
    val macro: Macro,
    val triggers: List<TriggerConfig>,
    val actions: List<ActionConfig>,
    val constraints: List<ConstraintConfig>,
)
