package com.openmacro.core.model

enum class VariableType {
    BOOLEAN,
    INTEGER,
    DECIMAL,
    STRING,
}

data class Variable(
    val id: Long = 0,
    val name: String,
    val type: VariableType = VariableType.STRING,
    val valueJson: String = "\"\"",
    val isGlobal: Boolean = true,
)
