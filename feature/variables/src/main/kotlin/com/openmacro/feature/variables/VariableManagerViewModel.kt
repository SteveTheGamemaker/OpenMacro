package com.openmacro.feature.variables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.engine.variable.VariableStore
import com.openmacro.core.model.Variable
import com.openmacro.core.model.VariableType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VariableManagerViewModel @Inject constructor(
    private val variableStore: VariableStore,
) : ViewModel() {

    val variables: StateFlow<List<Variable>> = variableStore.observeGlobals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addOrUpdate(name: String, value: String, type: VariableType) {
        if (name.isBlank()) return
        variableStore.setGlobal(name, value, type)
    }

    fun delete(name: String) {
        variableStore.deleteGlobal(name)
    }

    fun clearAll() {
        variableStore.clearGlobals()
    }
}
