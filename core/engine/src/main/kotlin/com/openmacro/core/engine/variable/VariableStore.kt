package com.openmacro.core.engine.variable

import com.openmacro.core.database.repository.VariableRepository
import com.openmacro.core.model.Variable
import com.openmacro.core.model.VariableType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory variable store backed by Room. Global variables persist across
 * app restarts; local variables live only in ExecutionContext.
 */
@Singleton
class VariableStore @Inject constructor(
    private val repository: VariableRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val globals = ConcurrentHashMap<String, Variable>()
    private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true
        scope.launch {
            repository.observeGlobals().collect { vars ->
                globals.clear()
                vars.forEach { globals[it.name] = it }
            }
        }
    }

    fun getGlobal(name: String): String? = globals[name]?.valueJson

    fun getGlobalVariable(name: String): Variable? = globals[name]

    fun getAllGlobals(): List<Variable> = globals.values.toList()

    fun observeAll(): Flow<List<Variable>> = repository.observeAll()

    fun observeGlobals(): Flow<List<Variable>> = repository.observeGlobals()

    fun setGlobal(name: String, value: String, type: VariableType = VariableType.STRING) {
        val existing = globals[name]
        val variable = Variable(
            id = existing?.id ?: 0,
            name = name,
            type = type,
            valueJson = value,
            isGlobal = true,
        )
        globals[name] = variable
        scope.launch { repository.upsert(variable) }
    }

    fun deleteGlobal(name: String) {
        globals.remove(name)
        scope.launch { repository.deleteByName(name) }
    }

    fun clearGlobals() {
        globals.clear()
        scope.launch { repository.clearGlobals() }
    }
}
