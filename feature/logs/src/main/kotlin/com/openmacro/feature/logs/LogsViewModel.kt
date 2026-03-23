package com.openmacro.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.model.MacroLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: MacroRepository,
) : ViewModel() {

    val logs: StateFlow<List<MacroLog>> = repository.observeRecentLogs(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearLogs() {
        viewModelScope.launch {
            repository.deleteAllLogs()
        }
    }
}
