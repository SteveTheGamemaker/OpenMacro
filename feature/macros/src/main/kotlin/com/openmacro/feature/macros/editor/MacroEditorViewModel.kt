package com.openmacro.feature.macros.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ActionType
import com.openmacro.core.model.ConstraintConfig
import com.openmacro.core.model.Macro
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.TriggerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MacroEditorUiState(
    val macroId: Long? = null,
    val name: String = "",
    val triggers: List<TriggerConfig> = emptyList(),
    val actions: List<ActionConfig> = emptyList(),
    val constraints: List<ConstraintConfig> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class MacroEditorViewModel @Inject constructor(
    private val repository: MacroRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroEditorUiState())
    val uiState: StateFlow<MacroEditorUiState> = _uiState.asStateFlow()

    private val macroId: Long? = savedStateHandle.get<Long>("macroId")?.takeIf { it != -1L }

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

    fun addAction(type: ActionType, configJson: String = "{}") {
        _uiState.update { state ->
            state.copy(
                actions = state.actions + ActionConfig(
                    macroId = state.macroId ?: 0,
                    typeId = type.typeId,
                    configJson = configJson,
                    sortOrder = state.actions.size,
                ),
            )
        }
    }

    fun removeAction(index: Int) {
        _uiState.update { state ->
            state.copy(actions = state.actions.toMutableList().apply { removeAt(index) })
        }
    }

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

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            val macro = Macro(
                id = state.macroId ?: 0,
                name = state.name.trim(),
            )
            repository.saveMacroWithDetails(
                macro = macro,
                triggers = state.triggers,
                actions = state.actions,
                constraints = state.constraints,
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
