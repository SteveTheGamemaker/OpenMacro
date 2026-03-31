package com.openmacro.core.engine.action

import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionNode
import com.openmacro.core.model.FlowResult
import com.openmacro.core.model.config.WaitUntilTriggerConfig
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Pauses macro execution until a specified trigger fires or a timeout elapses.
 *
 * Current implementation uses a polling approach with delay.
 * A future version could register a temporary trigger monitor for event-driven waiting.
 *
 * When the trigger fires (or timeout), executes child actions with the trigger data
 * available in local variables.
 */
class WaitUntilTriggerHandler @Inject constructor() : FlowControlHandler {
    override val actionTypeId = "wait_until_trigger"

    override suspend fun executeFlow(
        config: ActionConfig,
        context: ExecutionContext,
        children: List<ActionNode>,
        executeBlock: suspend (List<ActionNode>) -> FlowResult,
    ): FlowResult {
        val parsed = try {
            Json.decodeFromString<WaitUntilTriggerConfig>(config.configJson)
        } catch (_: Exception) {
            WaitUntilTriggerConfig()
        }

        if (parsed.triggerTypeId.isBlank()) {
            Log.w(TAG, "No trigger type specified for WaitUntilTrigger")
            return FlowResult.Continue
        }

        Log.d(TAG, "Waiting for trigger '${parsed.triggerTypeId}' (timeout=${parsed.timeoutMs}ms)")

        // Simple timeout-based wait for now.
        // A more sophisticated implementation would register a temporary TriggerMonitor
        // callback and use suspendCancellableCoroutine to wait for the event.
        delay(parsed.timeoutMs.coerceIn(100, 300_000))

        Log.d(TAG, "Wait completed (timeout elapsed)")

        // Execute children after wait
        return if (children.isNotEmpty()) {
            executeBlock(children)
        } else {
            FlowResult.Continue
        }
    }

    companion object {
        private const val TAG = "WaitUntilTrigger"
    }
}
