package com.openmacro.core.model

data class MacroCategory(
    val id: Long = 0,
    val name: String,
    val color: Int = DEFAULT_COLOR,
    val iconName: String = DEFAULT_ICON,
    val sortOrder: Int = 0,
) {
    companion object {
        const val DEFAULT_COLOR = 0xFF6750A4.toInt() // Material Purple
        const val DEFAULT_ICON = "folder"
    }
}
