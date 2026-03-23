package com.openmacro.core.engine.constraint

import android.content.Context

/**
 * Base interface for all constraint checkers. Each implementation evaluates
 * one constraint type and returns whether the constraint is satisfied.
 */
interface ConstraintChecker {
    val constraintTypeId: String

    suspend fun evaluate(configJson: String, context: Context): Boolean
}
