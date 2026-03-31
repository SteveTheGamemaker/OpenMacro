package com.openmacro.core.model

/**
 * A reusable group of actions (like a function/subroutine).
 * Can be referenced from macros via the RunActionBlock action.
 * Input/output parameters enable data flow between caller and block.
 */
data class ActionBlock(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val inputParams: List<String> = emptyList(),
    val outputParams: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
