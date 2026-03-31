package com.openmacro.feature.actionblocks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.ActionBlockRepository
import com.openmacro.core.engine.variable.VariableStore
import com.openmacro.core.model.ActionBlock
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionDisplayItem
import com.openmacro.core.model.ActionType
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

data class ActionBlockEditorUiState(
    val blockId: Long? = null,
    val name: String = "",
    val description: String = "",
    val inputParams: List<String> = emptyList(),
    val outputParams: List<String> = emptyList(),
    val actions: List<ActionConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
) {
    val actionDisplayItems: List<ActionDisplayItem>
        get() = buildActionDisplayList(buildActionTree(actions))
}

private val CONTAINER_TYPES = setOf(
    ActionType.IF_CLAUSE.typeId,
    ActionType.REPEAT.typeId,
    ActionType.ITERATE.typeId,
    ActionType.WAIT_UNTIL_TRIGGER.typeId,
)

@HiltViewModel
class ActionBlockEditorViewModel @Inject constructor(
    private val repository: ActionBlockRepository,
    private val variableStore: VariableStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActionBlockEditorUiState())
    val uiState: StateFlow<ActionBlockEditorUiState> = _uiState.asStateFlow()

    val userVariables: StateFlow<List<Pair<String, String>>> = variableStore.observeGlobals()
        .map { vars -> vars.map { it.name to it.type.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val blockId: Long? = savedStateHandle.get<Long>("blockId")?.takeIf { it != -1L }

    private val tempIdCounter = AtomicLong(-1)
    private fun nextTempId(): Long = tempIdCounter.getAndDecrement()

    init {
        if (blockId != null) {
            loadBlock(blockId)
        }
    }

    private fun loadBlock(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val block = repository.getById(id)
            if (block != null) {
                val actions = repository.observeBlockActions(id).first()
                _uiState.update {
                    it.copy(
                        blockId = id,
                        name = block.name,
                        description = block.description,
                        inputParams = block.inputParams,
                        outputParams = block.outputParams,
                        actions = actions,
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

    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun addInputParam(param: String) {
        if (param.isBlank()) return
        _uiState.update { it.copy(inputParams = it.inputParams + param.trim()) }
    }

    fun removeInputParam(index: Int) {
        _uiState.update { it.copy(inputParams = it.inputParams.toMutableList().apply { removeAt(index) }) }
    }

    fun addOutputParam(param: String) {
        if (param.isBlank()) return
        _uiState.update { it.copy(outputParams = it.outputParams + param.trim()) }
    }

    fun removeOutputParam(index: Int) {
        _uiState.update { it.copy(outputParams = it.outputParams.toMutableList().apply { removeAt(index) }) }
    }

    fun addAction(type: ActionType, configJson: String = "{}", parentActionId: Long? = null) {
        _uiState.update { state ->
            val siblingCount = state.actions.count { it.parentActionId == parentActionId }
            val newAction = ActionConfig(
                id = nextTempId(),
                macroId = 0,
                typeId = type.typeId,
                configJson = configJson,
                sortOrder = siblingCount,
                parentActionId = parentActionId,
            )
            state.copy(actions = state.actions + newAction)
        }
    }

    fun addElseMarker(parentActionId: Long) {
        addAction(ActionType.ELSE_MARKER, parentActionId = parentActionId)
    }

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

    fun updateActionConfigById(actionId: Long, configJson: String) {
        _uiState.update { state ->
            state.copy(
                actions = state.actions.map {
                    if (it.id == actionId) it.copy(configJson = configJson) else it
                },
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            val block = ActionBlock(
                id = state.blockId ?: 0,
                name = state.name.trim(),
                description = state.description.trim(),
                inputParams = state.inputParams,
                outputParams = state.outputParams,
            )

            val reindexed = reindexActions(state.actions)
            repository.saveBlockWithActions(block, reindexed)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun reindexActions(actions: List<ActionConfig>): List<ActionConfig> {
        val grouped = actions.groupBy { it.parentActionId }
        return actions.map { action ->
            val siblings = grouped[action.parentActionId] ?: return@map action
            val sortIndex = siblings.indexOf(action)
            action.copy(sortOrder = sortIndex)
        }
    }
}
