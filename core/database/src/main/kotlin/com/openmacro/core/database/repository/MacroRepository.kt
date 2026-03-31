package com.openmacro.core.database.repository

import com.openmacro.core.database.dao.ActionConfigDao
import com.openmacro.core.database.dao.CategoryDao
import com.openmacro.core.database.dao.ConstraintConfigDao
import com.openmacro.core.database.dao.MacroDao
import com.openmacro.core.database.dao.MacroLogDao
import com.openmacro.core.database.dao.TriggerConfigDao
import com.openmacro.core.database.entity.ActionConfigEntity
import com.openmacro.core.database.entity.toDomain
import com.openmacro.core.database.entity.toEntity
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.ConstraintConfig
import com.openmacro.core.model.Macro
import com.openmacro.core.model.MacroCategory
import com.openmacro.core.model.MacroLog
import com.openmacro.core.model.MacroWithDetails
import com.openmacro.core.model.TriggerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacroRepository @Inject constructor(
    private val macroDao: MacroDao,
    private val categoryDao: CategoryDao,
    private val triggerConfigDao: TriggerConfigDao,
    private val actionConfigDao: ActionConfigDao,
    private val constraintConfigDao: ConstraintConfigDao,
    private val macroLogDao: MacroLogDao,
) {
    // ── Macros ──

    fun observeAllMacros(): Flow<List<Macro>> =
        macroDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeEnabledMacros(): Flow<List<Macro>> =
        macroDao.observeEnabled().map { list -> list.map { it.toDomain() } }

    fun observeAllWithDetails(): Flow<List<MacroWithDetails>> =
        macroDao.observeAllWithDetails().map { list ->
            list.map { rel ->
                MacroWithDetails(
                    macro = rel.macro.toDomain(),
                    triggers = rel.triggers.map { it.toDomain() },
                    actions = rel.actions.map { it.toDomain() },
                    constraints = rel.constraints.map { it.toDomain() },
                )
            }
        }

    fun observeWithDetails(macroId: Long): Flow<MacroWithDetails?> =
        macroDao.observeWithDetails(macroId).map { rel ->
            rel?.let {
                MacroWithDetails(
                    macro = it.macro.toDomain(),
                    triggers = it.triggers.map { t -> t.toDomain() },
                    actions = it.actions.map { a -> a.toDomain() },
                    constraints = it.constraints.map { c -> c.toDomain() },
                )
            }
        }

    fun observeEnabledWithDetails(): Flow<List<MacroWithDetails>> =
        macroDao.observeEnabledWithDetails().map { list ->
            list.map { rel ->
                MacroWithDetails(
                    macro = rel.macro.toDomain(),
                    triggers = rel.triggers.map { it.toDomain() },
                    actions = rel.actions.map { it.toDomain() },
                    constraints = rel.constraints.map { it.toDomain() },
                )
            }
        }

    suspend fun getMacroById(id: Long): Macro? =
        macroDao.getById(id)?.toDomain()

    suspend fun insertMacro(macro: Macro): Long =
        macroDao.insert(macro.toEntity())

    suspend fun updateMacro(macro: Macro) =
        macroDao.update(macro.toEntity())

    suspend fun deleteMacro(id: Long) =
        macroDao.deleteById(id)

    suspend fun setMacroEnabled(id: Long, enabled: Boolean) =
        macroDao.setEnabled(id, enabled)

    /**
     * Saves a macro with all its triggers, actions, and constraints in one operation.
     * Replaces existing children for the macro.
     *
     * For actions with parent-child relationships (flow control nesting), this uses
     * a two-pass insert: first insert all actions with id=0 to get their real DB IDs,
     * then update parentActionId references to point to the correct new IDs.
     */
    suspend fun saveMacroWithDetails(
        macro: Macro,
        triggers: List<TriggerConfig>,
        actions: List<ActionConfig>,
        constraints: List<ConstraintConfig>,
    ): Long {
        val macroId = macroDao.insert(macro.toEntity())

        // Clear existing children and re-insert
        triggerConfigDao.deleteByMacro(macroId)
        actionConfigDao.deleteByMacro(macroId)
        constraintConfigDao.deleteByMacro(macroId)

        triggerConfigDao.insertAll(triggers.map { it.copy(macroId = macroId).toEntity() })
        constraintConfigDao.insertAll(constraints.map { it.copy(macroId = macroId).toEntity() })

        // Two-pass insert for actions to preserve parent-child relationships.
        // The editor uses temp IDs (negative or in-memory) for unsaved actions,
        // so we need to map old IDs → new DB IDs then fix parentActionId pointers.
        val hasNesting = actions.any { it.parentActionId != null }
        if (!hasNesting) {
            // Fast path: no nesting, simple bulk insert
            actionConfigDao.insertAll(
                actions.map { it.copy(macroId = macroId, id = 0).toEntity() },
            )
        } else {
            // Pass 1: Insert all actions with parentActionId = null to get new DB IDs
            val actionsForInsert = actions.map {
                it.copy(macroId = macroId, id = 0, parentActionId = null).toEntity()
            }
            val newIds = actionConfigDao.insertAll(actionsForInsert)

            // Build mapping from old (temp) IDs → new DB IDs
            val oldToNewId = mutableMapOf<Long, Long>()
            actions.forEachIndexed { index, action ->
                oldToNewId[action.id] = newIds[index]
            }

            // Pass 2: Update parentActionId for actions that have a parent
            for ((index, action) in actions.withIndex()) {
                val oldParentId = action.parentActionId ?: continue
                val newParentId = oldToNewId[oldParentId] ?: continue
                val newId = newIds[index]
                actionConfigDao.update(
                    ActionConfigEntity(
                        id = newId,
                        macroId = macroId,
                        actionBlockId = action.actionBlockId,
                        type = action.typeId,
                        configJson = action.configJson,
                        sortOrder = action.sortOrder,
                        isEnabled = action.isEnabled,
                        parentActionId = newParentId,
                    ),
                )
            }
        }

        return macroId
    }

    // ── Categories ──

    fun observeAllCategories(): Flow<List<MacroCategory>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun insertCategory(category: MacroCategory): Long =
        categoryDao.insert(category.toEntity())

    suspend fun updateCategory(category: MacroCategory) =
        categoryDao.update(category.toEntity())

    suspend fun deleteCategory(category: MacroCategory) =
        categoryDao.delete(category.toEntity())

    // ── Triggers ──

    fun observeTriggers(macroId: Long): Flow<List<TriggerConfig>> =
        triggerConfigDao.observeByMacro(macroId).map { list -> list.map { it.toDomain() } }

    suspend fun insertTrigger(trigger: TriggerConfig): Long =
        triggerConfigDao.insert(trigger.toEntity())

    suspend fun updateTrigger(trigger: TriggerConfig) =
        triggerConfigDao.update(trigger.toEntity())

    suspend fun deleteTrigger(trigger: TriggerConfig) =
        triggerConfigDao.delete(trigger.toEntity())

    // ── Actions ──

    fun observeActions(macroId: Long): Flow<List<ActionConfig>> =
        actionConfigDao.observeByMacro(macroId).map { list -> list.map { it.toDomain() } }

    suspend fun insertAction(action: ActionConfig): Long =
        actionConfigDao.insert(action.toEntity())

    suspend fun updateAction(action: ActionConfig) =
        actionConfigDao.update(action.toEntity())

    suspend fun deleteAction(action: ActionConfig) =
        actionConfigDao.delete(action.toEntity())

    // ── Constraints ──

    fun observeConstraints(macroId: Long): Flow<List<ConstraintConfig>> =
        constraintConfigDao.observeByMacro(macroId).map { list -> list.map { it.toDomain() } }

    suspend fun insertConstraint(constraint: ConstraintConfig): Long =
        constraintConfigDao.insert(constraint.toEntity())

    suspend fun updateConstraint(constraint: ConstraintConfig) =
        constraintConfigDao.update(constraint.toEntity())

    suspend fun deleteConstraint(constraint: ConstraintConfig) =
        constraintConfigDao.delete(constraint.toEntity())

    // ── Logs ──

    fun observeAllLogs(): Flow<List<MacroLog>> =
        macroLogDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeRecentLogs(limit: Int = 100): Flow<List<MacroLog>> =
        macroLogDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    suspend fun insertLog(log: MacroLog): Long =
        macroLogDao.insert(log.toEntity())

    suspend fun completeLog(id: Long, completedAt: Long, status: String, errorMessage: String? = null) =
        macroLogDao.updateCompletion(id, completedAt, status, errorMessage)

    suspend fun deleteOldLogs(before: Long) =
        macroLogDao.deleteOlderThan(before)

    suspend fun deleteAllLogs() =
        macroLogDao.deleteAll()
}
