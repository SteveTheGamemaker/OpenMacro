package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a list of actions sequentially. If one action fails, the remaining
 * actions are skipped and the error is propagated.
 */
@Singleton
class ActionExecutor @Inject constructor(
    private val registry: ActionRegistry,
) {
    suspend fun execute(actions: List<ActionConfig>, context: ExecutionContext) {
        for (action in actions.filter { it.isEnabled }.sortedBy { it.sortOrder }) {
            val handler = registry.get(action.typeId)
            if (handler == null) {
                Log.w(TAG, "No handler for action type: ${action.typeId}")
                continue
            }
            handler.execute(action, context)
        }
    }

    companion object {
        private const val TAG = "ActionExecutor"
    }
}
