package com.openmacro.feature.macros

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openmacro.core.database.repository.MacroRepository
import com.openmacro.core.engine.service.MacroService
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.Macro
import com.openmacro.core.model.MacroWithDetails
import com.openmacro.core.model.TriggerConfig
import com.openmacro.core.model.config.DisplayNotificationConfig
import com.openmacro.core.model.config.ScreenOnOffConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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
            // Service will stop itself when no macros are enabled
        }
    }

    fun deleteMacro(macroId: Long) {
        viewModelScope.launch {
            repository.deleteMacro(macroId)
        }
    }

    fun createTestMacro() {
        viewModelScope.launch {
            val triggerJson = Json.encodeToString(
                ScreenOnOffConfig.serializer(),
                ScreenOnOffConfig(onScreenOn = true, onScreenOff = false),
            )
            val actionJson = Json.encodeToString(
                DisplayNotificationConfig.serializer(),
                DisplayNotificationConfig(
                    title = "Screen Unlocked",
                    body = "OpenMacro detected screen on!",
                ),
            )
            repository.saveMacroWithDetails(
                macro = Macro(name = "Test: Screen On → Notify"),
                triggers = listOf(
                    TriggerConfig(
                        macroId = 0,
                        typeId = "screen_on_off",
                        configJson = triggerJson,
                    ),
                ),
                actions = listOf(
                    ActionConfig(
                        macroId = 0,
                        typeId = "display_notification",
                        configJson = actionJson,
                    ),
                ),
                constraints = emptyList(),
            )
        }
    }
}
