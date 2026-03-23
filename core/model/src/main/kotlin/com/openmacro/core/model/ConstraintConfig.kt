package com.openmacro.core.model

data class ConstraintConfig(
    val id: Long = 0,
    val macroId: Long,
    val parentConstraintId: Long? = null,
    val typeId: String,
    val configJson: String = "{}",
    val logicOperator: LogicOperator = LogicOperator.AND,
    val sortOrder: Int = 0,
) {
    val type: ConstraintType? get() = ConstraintType.fromTypeId(typeId)
}
