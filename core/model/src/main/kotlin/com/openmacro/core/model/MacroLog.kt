package com.openmacro.core.model

enum class MacroLogStatus {
    SUCCESS,
    FAILURE,
    CANCELLED,
    CONSTRAINT_NOT_MET,
}

data class MacroLog(
    val id: Long = 0,
    val macroId: Long,
    val macroName: String,
    val triggerType: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: MacroLogStatus = MacroLogStatus.SUCCESS,
    val errorMessage: String? = null,
)
