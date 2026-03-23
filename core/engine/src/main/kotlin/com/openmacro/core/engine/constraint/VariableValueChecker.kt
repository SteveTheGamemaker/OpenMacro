package com.openmacro.core.engine.constraint

import android.content.Context
import com.openmacro.core.engine.variable.VariableStore
import com.openmacro.core.model.config.VariableValueConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class VariableValueChecker @Inject constructor(
    private val variableStore: VariableStore,
) : ConstraintChecker {
    override val constraintTypeId = "variable_value"

    override suspend fun evaluate(configJson: String, context: Context): Boolean {
        val config = Json.decodeFromString<VariableValueConfig>(configJson)
        val actual = variableStore.getGlobal(config.variableName) ?: return false
        val expected = config.value

        return when (config.operator) {
            "==" -> actual == expected
            "!=" -> actual != expected
            ">" -> compareNumeric(actual, expected) > 0
            "<" -> compareNumeric(actual, expected) < 0
            ">=" -> compareNumeric(actual, expected) >= 0
            "<=" -> compareNumeric(actual, expected) <= 0
            "contains" -> actual.contains(expected, ignoreCase = true)
            else -> false
        }
    }

    private fun compareNumeric(a: String, b: String): Int {
        val aNum = a.toDoubleOrNull()
        val bNum = b.toDoubleOrNull()
        return if (aNum != null && bNum != null) {
            aNum.compareTo(bNum)
        } else {
            a.compareTo(b)
        }
    }
}
