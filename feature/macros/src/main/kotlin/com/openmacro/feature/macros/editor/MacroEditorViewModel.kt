package com.openmacro.feature.macros.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.engine.variable.VariableStore
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionDisplayItem
import com.openmacro.core.model.ActionType
import com.openmacro.core.model.ConstraintConfig
import com.openmacro.core.model.ConstraintType
import com.openmacro.core.model.LogicOperator
import com.openmacro.core.model.Macro
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.TriggerType
import com.openmacro.core.model.buildActionDisplayList
import com.openmacro.core.model.buildActionTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

data class MacroEditorUiState(
    val macroId: Long? = null,
    val name: String = "",
    val triggers: List<TriggerConfig> = emptyList(),
    val actions: List<ActionConfig> = emptyList(),
    val constraints: List<ConstraintConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
) {
    /** Actions displayed as a flat list with depth info for indented rendering. */
    val actionDisplayItems: List<ActionDisplayItem>
        get() = buildActionDisplayList(buildActionTree(actions))
}

/** Types that can contain child actions. */
private val CONTAINER_TYPES = setOf(
    ActionType.IF_CLAUSE.typeId,
    ActionType.REPEAT.typeId,
    ActionType.ITERATE.typeId,
    ActionType.WAIT_UNTIL_TRIGGER.typeId,
)

fun ActionConfig.isContainer(): Boolean = typeId in CONTAINER_TYPES

@HiltViewModel
class MacroEditorViewModel @Inject constructor(
    private val repository: MacroRepository,
    private val variableStore: VariableStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroEditorUiState())
    val uiState: StateFlow<MacroEditorUiState> = _uiState.asStateFlow()

    /** User-defined variables as (name, type) pairs for the magic text picker. */
    val userVariables: StateFlow<List<Pair<String, String>>> = variableStore.observeGlobals()
        .map { vars -> vars.map { it.name to it.type.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val macroId: Long? = savedStateHandle.get<Long>("macroId")?.takeIf { it != -1L }

    /**
     * Temp ID counter for unsaved actions. Uses negative values to distinguish
     * from real DB IDs. These are mapped to real IDs during save.
     */
    private val tempIdCounter = AtomicLong(-1)
    private fun nextTempId(): Long = tempIdCounter.getAndDecrement()

    init {
        if (macroId != null) {
            loadMacro(macroId)
        }
    }

    private fun loadMacro(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val details = repository.observeWithDetails(id).first()
            if (details != null) {
                // Assign temp IDs to loaded actions so parent-child relationships
                // work correctly in the editor (original DB IDs are kept)
                _uiState.update {
                    it.copy(
                        macroId = id,
                        name = details.macro.name,
                        triggers = details.triggers,
                        actions = details.actions,
                        constraints = details.constraints,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun addTrigger(type: TriggerType, configJson: String = "{}") {
        _uiState.update { state ->
            state.copy(
                triggers = state.triggers + TriggerConfig(
                    macroId = state.macroId ?: 0,
                    typeId = type.typeId,
                    configJson = configJson,
                    sortOrder = state.triggers.size,
                ),
            )
        }
    }

    fun removeTrigger(index: Int) {
        _uiState.update { state ->
            state.copy(triggers = state.triggers.toMutableList().apply { removeAt(index) })
        }
    }

    fun updateTriggerConfig(index: Int, configJson: String) {
        _uiState.update { state ->
            state.copy(
                triggers = state.triggers.toMutableList().apply {
                    set(index, get(index).copy(configJson = configJson))
                },
            )
        }
    }

    /**
     * Add an action at root level or as a child of a parent container action.
     * @param parentActionId If non-null, the new action is nested inside this parent.
     */
    fun addAction(type: ActionType, configJson: String = "{}", parentActionId: Long? = null) {
        _uiState.update { state ->
            val siblingCount = state.actions.count { it.parentActionId == parentActionId }
            val newAction = ActionConfig(
                id = nextTempId(),
                macroId = state.macroId ?: 0,
                typeId = type.typeId,
                configJson = configJson,
                sortOrder = siblingCount,
                parentActionId = parentActionId,
            )
            state.copy(actions = state.actions + newAction)
        }
    }

    /**
     * Add an Else marker inside an If block.
     */
    fun addElseMarker(parentActionId: Long) {
        addAction(ActionType.ELSE_MARKER, parentActionId = parentActionId)
    }

    /**
     * Remove an action and all its descendants.
     */
    fun removeAction(actionId: Long) {
        _uiState.update { state ->
            val toRemove = collectDescendants(state.actions, actionId) + actionId
            state.copy(actions = state.actions.filter { it.id !in toRemove })
        }
    }

    private fun collectDescendants(actions: List<ActionConfig>, parentId: Long): Set<Long> {
        val result = mutableSetOf<Long>()
        val children = actions.filter { it.parentActionId == parentId }
        for (child in children) {
            result.add(child.id)
            result.addAll(collectDescendants(actions, child.id))
        }
        return result
    }

    /**
     * Update an action's config by its ID (not index, since display order != storage order).
     */
    fun updateActionConfigById(actionId: Long, configJson: String) {
        _uiState.update { state ->
            state.copy(
                actions = state.actions.map {
                    if (it.id == actionId) it.copy(configJson = configJson) else it
                },
            )
        }
    }

    /** Legacy index-based update for compatibility. */
    fun updateActionConfig(index: Int, configJson: String) {
        _uiState.update { state ->
            state.copy(
                actions = state.actions.toMutableList().apply {
                    set(index, get(index).copy(configJson = configJson))
                },
            )
        }
    }

    fun moveAction(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutable = state.actions.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            state.copy(actions = mutable.mapIndexed { i, a -> a.copy(sortOrder = i) })
        }
    }

    fun addConstraint(type: ConstraintType, configJson: String = "{}") {
        _uiState.update { state ->
            state.copy(
                constraints = state.constraints + ConstraintConfig(
                    macroId = state.macroId ?: 0,
                    typeId = type.typeId,
                    configJson = configJson,
                    logicOperator = if (state.constraints.isEmpty()) LogicOperator.AND else LogicOperator.AND,
                    sortOrder = state.constraints.size,
                ),
            )
        }
    }

    fun removeConstraint(index: Int) {
        _uiState.update { state ->
            state.copy(constraints = state.constraints.toMutableList().apply { removeAt(index) })
        }
    }

    fun updateConstraintConfig(index: Int, configJson: String) {
        _uiState.update { state ->
            state.copy(
                constraints = state.constraints.toMutableList().apply {
                    set(index, get(index).copy(configJson = configJson))
                },
            )
        }
    }

    fun updateConstraintLogicOperator(index: Int, operator: LogicOperator) {
        _uiState.update { state ->
            state.copy(
                constraints = state.constraints.toMutableList().apply {
                    set(index, get(index).copy(logicOperator = operator))
                },
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            val macro = Macro(
                id = state.macroId ?: 0,
                name = state.name.trim(),
            )

            // Re-index sortOrders to be correct per parent group
            val reindexed = reindexActions(state.actions)

            repository.saveMacroWithDetails(
                macro = macro,
                triggers = state.triggers,
                actions = reindexed,
                constraints = state.constraints,
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    /**
     * Re-indexes actions so each group of siblings (same parentActionId)
     * has sequential sortOrder values starting from 0.
     */
    private fun reindexActions(actions: List<ActionConfig>): List<ActionConfig> {
        val grouped = actions.groupBy { it.parentActionId }
        return actions.map { action ->
            val siblings = grouped[action.parentActionId] ?: return@map action
            val sortIndex = siblings.indexOf(action)
            action.copy(sortOrder = sortIndex)
        }
    }
}
