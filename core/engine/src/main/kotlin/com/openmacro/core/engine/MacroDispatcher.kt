package com.openmacro.core.engine

import android.content.Context
import android.util.Log
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.engine.action.ActionExecutor
import com.openmacro.core.engine.constraint.ConstraintEvaluator
import com.openmacro.core.engine.trigger.TriggerRegistry
import com.openmacro.core.engine.variable.VariableStore
import com.openmacro.core.model.MacroLog
import com.openmacro.core.model.MacroLogStatus
import com.openmacro.core.model.MacroWithDetails
import com.openmacro.core.model.TriggerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central orchestrator. Observes enabled macros, starts/stops trigger monitors,
 * evaluates constraints, and dispatches trigger events to the action executor.
 *
 * Web analogy: this is like an Express router — events come in, it matches them
 * to the right handler (macro), runs middleware (constraints), then the handler chain (actions).
 */
@Singleton
class MacroDispatcher @Inject constructor(
    private val repository: MacroRepository,
    private val triggerRegistry: TriggerRegistry,
    private val actionExecutor: ActionExecutor,
    private val constraintEvaluator: ConstraintEvaluator,
    private val variableStore: VariableStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var appContext: Context? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var currentMacros: List<MacroWithDetails> = emptyList()

    fun start(context: Context) {
        if (_isRunning.value) return
        appContext = context.applicationContext
        _isRunning.value = true

        variableStore.initialize()

        scope.launch {
            repository.observeEnabledWithDetails().collect { macros ->
                currentMacros = macros
                refreshTriggers(macros)
            }
        }

        Log.i(TAG, "MacroDispatcher started")
    }

    fun stop() {
        if (!_isRunning.value) return
        _isRunning.value = false

        for (monitor in triggerRegistry.all()) {
            monitor.stop()
        }
        currentMacros = emptyList()

        Log.i(TAG, "MacroDispatcher stopped")
    }

    private fun refreshTriggers(macros: List<MacroWithDetails>) {
        val ctx = appContext ?: return

        // Group all trigger configs by typeId
        val configsByType = mutableMapOf<String, MutableList<TriggerConfig>>()
        for (macro in macros) {
            for (trigger in macro.triggers.filter { it.isEnabled }) {
                configsByType.getOrPut(trigger.typeId) { mutableListOf() }.add(trigger)
            }
        }

        // Start monitors that have configs, stop ones that don't
        for (monitor in triggerRegistry.all()) {
            val configs = configsByType[monitor.triggerTypeId]
            if (configs.isNullOrEmpty()) {
                monitor.stop()
            } else {
                monitor.start(ctx, configs, ::onTriggerEvent)
            }
        }
    }

    private fun onTriggerEvent(event: TriggerEvent) {
        scope.launch {
            handleTrigger(event)
        }
    }

    private suspend fun handleTrigger(event: TriggerEvent) {
        val ctx = appContext ?: return

        // Find all enabled macros that have a trigger matching this event type
        val matchingMacros = currentMacros.filter { macro ->
            macro.triggers.any { it.typeId == event.triggerTypeId && it.isEnabled }
        }

        for (macro in matchingMacros) {
            executeMacro(ctx, macro, event)
        }
    }

    private suspend fun executeMacro(
        context: Context,
        macro: MacroWithDetails,
        event: TriggerEvent,
    ) {
        val log = MacroLog(
            macroId = macro.macro.id,
            macroName = macro.macro.name,
            triggerType = event.triggerTypeId,
        )
        val logId = repository.insertLog(log)

        try {
            // Evaluate constraints before executing actions
            if (macro.constraints.isNotEmpty()) {
                val constraintsMet = constraintEvaluator.evaluate(macro.constraints, context)
                if (!constraintsMet) {
                    repository.completeLog(
                        id = logId,
                        completedAt = System.currentTimeMillis(),
                        status = MacroLogStatus.CONSTRAINT_NOT_MET.name,
                    )
                    Log.d(TAG, "Macro '${macro.macro.name}' constraints not met, skipping")
                    return
                }
            }

            val execContext = ExecutionContext(
                androidContext = context,
                triggerEvent = event,
                logId = logId,
                macroId = macro.macro.id,
                macroName = macro.macro.name,
                variableStore = variableStore,
            )

            actionExecutor.execute(macro.actions, execContext)

            repository.completeLog(
                id = logId,
                completedAt = System.currentTimeMillis(),
                status = MacroLogStatus.SUCCESS.name,
            )
            Log.d(TAG, "Macro '${macro.macro.name}' executed successfully")
        } catch (e: Exception) {
            repository.completeLog(
                id = logId,
                completedAt = System.currentTimeMillis(),
                status = MacroLogStatus.FAILURE.name,
                errorMessage = e.message,
            )
            Log.e(TAG, "Macro '${macro.macro.name}' failed", e)
        }
    }

    companion object {
        private const val TAG = "MacroDispatcher"
    }
}
