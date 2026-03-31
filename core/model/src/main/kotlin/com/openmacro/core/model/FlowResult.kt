package com.openmacro.core.model

/**
 * Signals returned by action execution to control flow.
 * The executor checks this after each action to decide whether to continue,
 * break out of a loop, skip to the next iteration, or cancel the entire macro.
 */
sealed class FlowResult {
    /** Normal completion — continue to the next action. */
    data object Continue : FlowResult()

    /** Break out of the nearest enclosing loop. */
    data object Break : FlowResult()

    /** Skip to the next iteration of the nearest enclosing loop. */
    data object ContinueLoop : FlowResult()

    /** Cancel all remaining actions in the macro. */
    data object CancelMacro : FlowResult()
}
