package com.openmacro.core.database.entity

import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ConstraintConfig
import com.openmacro.core.model.LogicOperator
import com.openmacro.core.model.Macro
import com.openmacro.core.model.MacroCategory
import com.openmacro.core.model.MacroLog
import com.openmacro.core.model.MacroLogStatus
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.Variable
import com.openmacro.core.model.VariableType

// ── Macro ──

fun MacroEntity.toDomain() = Macro(
    id = id,
    name = name,
    categoryId = categoryId,
    isEnabled = isEnabled,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Macro.toEntity() = MacroEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    isEnabled = isEnabled,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// ── Category ──

fun CategoryEntity.toDomain() = MacroCategory(
    id = id,
    name = name,
    color = color,
    iconName = iconName,
    sortOrder = sortOrder,
)

fun MacroCategory.toEntity() = CategoryEntity(
    id = id,
    name = name,
    color = color,
    iconName = iconName,
    sortOrder = sortOrder,
)

// ── TriggerConfig ──

fun TriggerConfigEntity.toDomain() = TriggerConfig(
    id = id,
    macroId = macroId,
    typeId = type,
    configJson = configJson,
    isEnabled = isEnabled,
    sortOrder = sortOrder,
)

fun TriggerConfig.toEntity() = TriggerConfigEntity(
    id = id,
    macroId = macroId,
    type = typeId,
    configJson = configJson,
    isEnabled = isEnabled,
    sortOrder = sortOrder,
)

// ── ActionConfig ──

fun ActionConfigEntity.toDomain() = ActionConfig(
    id = id,
    macroId = macroId,
    actionBlockId = actionBlockId,
    typeId = type,
    configJson = configJson,
    sortOrder = sortOrder,
    isEnabled = isEnabled,
    parentActionId = parentActionId,
)

fun ActionConfig.toEntity() = ActionConfigEntity(
    id = id,
    macroId = macroId,
    actionBlockId = actionBlockId,
    type = typeId,
    configJson = configJson,
    sortOrder = sortOrder,
    isEnabled = isEnabled,
    parentActionId = parentActionId,
)

// ── ConstraintConfig ──

fun ConstraintConfigEntity.toDomain() = ConstraintConfig(
    id = id,
    macroId = macroId,
    parentConstraintId = parentConstraintId,
    typeId = type,
    configJson = configJson,
    logicOperator = LogicOperator.valueOf(logicOperator),
    sortOrder = sortOrder,
)

fun ConstraintConfig.toEntity() = ConstraintConfigEntity(
    id = id,
    macroId = macroId,
    parentConstraintId = parentConstraintId,
    type = typeId,
    configJson = configJson,
    logicOperator = logicOperator.name,
    sortOrder = sortOrder,
)

// ── MacroLog ──

fun MacroLogEntity.toDomain() = MacroLog(
    id = id,
    macroId = macroId,
    macroName = macroName,
    triggerType = triggerType,
    startedAt = startedAt,
    completedAt = completedAt,
    status = MacroLogStatus.valueOf(status),
    errorMessage = errorMessage,
)

fun MacroLog.toEntity() = MacroLogEntity(
    id = id,
    macroId = macroId,
    macroName = macroName,
    triggerType = triggerType,
    startedAt = startedAt,
    completedAt = completedAt,
    status = status.name,
    errorMessage = errorMessage,
)

// ── Variable ──

fun VariableEntity.toDomain() = Variable(
    id = id,
    name = name,
    type = VariableType.valueOf(type),
    valueJson = valueJson,
    isGlobal = isGlobal,
)

fun Variable.toEntity() = VariableEntity(
    id = id,
    name = name,
    type = type.name,
    valueJson = valueJson,
    isGlobal = isGlobal,
)
