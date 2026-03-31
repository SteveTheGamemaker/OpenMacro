package com.openmacro.core.model

/**
 * A tree node wrapping an [ActionConfig] with its child actions.
 * Used by the executor to walk nested action structures (if/else, loops, etc.)
 * and by the editor UI to render indented action lists.
 */
data class ActionNode(
    val action: ActionConfig,
    val children: List<ActionNode> = emptyList(),
)

/**
 * Builds a tree of [ActionNode]s from a flat list of [ActionConfig]s.
 * Root-level actions have `parentActionId == null`. Children are grouped
 * under their parent and sorted by [ActionConfig.sortOrder].
 */
fun buildActionTree(actions: List<ActionConfig>): List<ActionNode> {
    val byParent = actions.groupBy { it.parentActionId }

    fun buildLevel(parentId: Long?): List<ActionNode> =
        (byParent[parentId] ?: emptyList())
            .sortedBy { it.sortOrder }
            .map { ActionNode(it, buildLevel(it.id)) }

    return buildLevel(null)
}

/**
 * Flattens a tree of [ActionNode]s back to a flat list of [ActionConfig]s.
 * Useful for persisting to the database.
 */
fun flattenActionTree(nodes: List<ActionNode>): List<ActionConfig> {
    val result = mutableListOf<ActionConfig>()
    fun walk(nodeList: List<ActionNode>) {
        for (node in nodeList) {
            result.add(node.action)
            walk(node.children)
        }
    }
    walk(nodes)
    return result
}

/**
 * Flattens a tree into a display list with depth information.
 * Used by the editor UI to render indented actions in a flat LazyColumn.
 */
data class ActionDisplayItem(
    val action: ActionConfig,
    val depth: Int,
    val hasChildren: Boolean,
)

fun buildActionDisplayList(nodes: List<ActionNode>, depth: Int = 0): List<ActionDisplayItem> {
    val result = mutableListOf<ActionDisplayItem>()
    for (node in nodes) {
        result.add(ActionDisplayItem(node.action, depth, node.children.isNotEmpty()))
        result.addAll(buildActionDisplayList(node.children, depth + 1))
    }
    return result
}
