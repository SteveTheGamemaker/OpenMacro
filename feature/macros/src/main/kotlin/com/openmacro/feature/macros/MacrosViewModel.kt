package com.openmacro.feature.macros

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.engine.service.MacroService
import com.openmacro.core.model.MacroWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MacrosViewModel @Inject constructor(
    private val repository: MacroRepository,
) : ViewModel() {

    val macros: StateFlow<List<MacroWithDetails>> = repository.observeAllWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMacro(macroId: Long, enabled: Boolean, context: Context) {
        viewModelScope.launch {
            repository.setMacroEnabled(macroId, enabled)
            if (enabled) {
                MacroService.start(context)
            }
        }
    }

    fun deleteMacro(macroId: Long) {
        viewModelScope.launch {
            repository.deleteMacro(macroId)
        }
    }
}
