package com.openmacro.core.engine.constraint

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of all available constraint checkers, keyed by constraintTypeId.
 */
@Singleton
class ConstraintRegistry @Inject constructor(
    checkers: Set<@JvmSuppressWildcards ConstraintChecker>,
) {
    private val byTypeId: Map<String, ConstraintChecker> =
        checkers.associateBy { it.constraintTypeId }

    fun get(typeId: String): ConstraintChecker? = byTypeId[typeId]
}
