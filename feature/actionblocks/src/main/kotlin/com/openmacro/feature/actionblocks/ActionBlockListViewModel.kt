package com.openmacro.feature.actionblocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.ActionBlockRepository
import com.openmacro.core.model.ActionBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActionBlockListViewModel @Inject constructor(
    private val repository: ActionBlockRepository,
) : ViewModel() {

    val blocks: StateFlow<List<ActionBlock>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
