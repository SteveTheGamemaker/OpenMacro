package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.engine.text.MagicTextResolver
import com.openmacro.core.model.ActionConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes a list of actions sequentially. If one action fails, the remaining
 * actions are skipped and the error is propagated.
 *
 * Magic Text tokens in configJson are resolved before passing to handlers,
 * so handlers always receive fully-resolved config values.
 */
@Singleton
class ActionExecutor @Inject constructor(
    private val registry: ActionRegistry,
    private val magicTextResolver: MagicTextResolver,
) {
    suspend fun execute(actions: List<ActionConfig>, context: ExecutionContext) {
        for (action in actions.filter { it.isEnabled }.sortedBy { it.sortOrder }) {
            val handler = registry.get(action.typeId)
            if (handler == null) {
                Log.w(TAG, "No handler for action type: ${action.typeId}")
                continue
            }
            // Resolve magic text tokens in the config JSON before execution
            val resolvedAction = action.copy(
                configJson = magicTextResolver.resolve(action.configJson, context),
            )
            handler.execute(resolvedAction, context)
        }
    }

    companion object {
        private const val TAG = "ActionExecutor"
    }
}
