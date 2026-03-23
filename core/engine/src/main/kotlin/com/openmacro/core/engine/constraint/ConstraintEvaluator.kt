package com.openmacro.core.engine.constraint

import android.content.Context
import android.util.Log
import com.openmacro.core.model.ConstraintConfig
import com.openmacro.core.model.LogicOperator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evaluates a list of constraints using their logic operators.
 *
 * The first constraint seeds the result. Each subsequent constraint combines
 * its individual result with the running result using its logicOperator.
 *
 * NOT inverts the individual constraint result before combining with AND.
 * An empty constraint list returns true (no constraints = always pass).
 */
@Singleton
class ConstraintEvaluator @Inject constructor(
    private val registry: ConstraintRegistry,
) {
    suspend fun evaluate(constraints: List<ConstraintConfig>, context: Context): Boolean {
        val sorted = constraints.sortedBy { it.sortOrder }
        if (sorted.isEmpty()) return true

        var result = evaluateSingle(sorted.first(), context)

        for (i in 1 until sorted.size) {
            val constraint = sorted[i]
            val value = evaluateSingle(constraint, context)

            result = when (constraint.logicOperator) {
                LogicOperator.AND -> result && value
                LogicOperator.OR -> result || value
                LogicOperator.XOR -> result xor value
                LogicOperator.NOT -> result && !value
            }
        }

        return result
    }

    private suspend fun evaluateSingle(constraint: ConstraintConfig, context: Context): Boolean {
        val checker = registry.get(constraint.typeId)
        if (checker == null) {
            Log.w(TAG, "No checker for constraint type: ${constraint.typeId}")
            return true // unknown constraints pass by default
        }
        return try {
            checker.evaluate(constraint.configJson, context)
        } catch (e: Exception) {
            Log.e(TAG, "Constraint ${constraint.typeId} threw exception", e)
            false
        }
    }

    companion object {
        private const val TAG = "ConstraintEvaluator"
    }
}
