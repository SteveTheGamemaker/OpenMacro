package com.openmacro.core.engine.expression

/**
 * The possible result types of evaluating an expression.
 */
sealed class ExpressionValue {
    data class NumberVal(val value: Double) : ExpressionValue()
    data class StringVal(val value: String) : ExpressionValue()
    data class BoolVal(val value: Boolean) : ExpressionValue()

    /** Coerce to a boolean for conditions. */
    fun toBoolean(): Boolean = when (this) {
        is BoolVal -> value
        is NumberVal -> value != 0.0
        is StringVal -> value.isNotEmpty() && value != "false"
    }

    /** Coerce to a number for arithmetic. */
    fun toNumber(): Double = when (this) {
        is NumberVal -> value
        is BoolVal -> if (value) 1.0 else 0.0
        is StringVal -> value.toDoubleOrNull()
            ?: throw ExpressionException("Cannot convert '$value' to number")
    }

    /** Coerce to a string for display/concatenation. */
    fun toStringVal(): String = when (this) {
        is StringVal -> value
        is NumberVal -> if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        is BoolVal -> value.toString()
    }

    override fun toString(): String = toStringVal()
}

class ExpressionException(message: String) : RuntimeException(message)
