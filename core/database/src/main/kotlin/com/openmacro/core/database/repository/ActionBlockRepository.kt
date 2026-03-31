package com.openmacro.core.database.repository

import com.openmacro.core.database.dao.ActionBlockDao
import com.openmacro.core.database.dao.ActionConfigDao
import com.openmacro.core.database.entity.toDomain
import com.openmacro.core.database.entity.toEntity
import com.openmacro.core.model.ActionBlock
import com.openmacro.core.model.ActionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionBlockRepository @Inject constructor(
    private val actionBlockDao: ActionBlockDao,
    private val actionConfigDao: ActionConfigDao,
) {
    fun observeAll(): Flow<List<ActionBlock>> =
        actionBlockDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): ActionBlock? =
        actionBlockDao.getById(id)?.toDomain()

    suspend fun insert(block: ActionBlock): Long =
        actionBlockDao.insert(block.toEntity())

    suspend fun update(block: ActionBlock) =
        actionBlockDao.update(block.toEntity())

    suspend fun delete(id: Long) =
        actionBlockDao.deleteById(id)

    /**
     * Get actions belonging to an action block (stored with actionBlockId set).
     */
    fun observeBlockActions(blockId: Long): Flow<List<ActionConfig>> =
        actionConfigDao.observeByActionBlock(blockId).map { list -> list.map { it.toDomain() } }

    /**
     * Save an action block along with its actions (delete-and-reinsert for actions).
     * Uses two-pass insert for nested actions (same as MacroRepository).
     */
    suspend fun saveBlockWithActions(block: ActionBlock, actions: List<ActionConfig>): Long {
        val blockId = if (block.id == 0L) {
            insert(block)
        } else {
            update(block.copy(updatedAt = System.currentTimeMillis()))
            block.id
        }

        // Clear existing actions for this block
        actionConfigDao.deleteByActionBlock(blockId)

        if (actions.isEmpty()) return blockId

        // Check if any actions have nesting
        val hasNesting = actions.any { it.parentActionId != null }

        if (!hasNesting) {
            // Fast path: no nesting, just insert all with blockId set
            val entities = actions.mapIndexed { i, a ->
                a.copy(
                    id = 0,
                    macroId = 0,
                    actionBlockId = blockId,
                    sortOrder = i,
                ).toEntity()
            }
            actionConfigDao.insertAll(entities)
        } else {
            // Two-pass: insert with null parentActionId to get IDs, then update
            val oldIdToNew = mutableMapOf<Long, Long>()

            // Pass 1: insert all actions without parent references
            val entities = actions.map { a ->
                a.copy(
                    id = 0,
                    macroId = 0,
                    actionBlockId = blockId,
                    parentActionId = null,
                ).toEntity()
            }
            val newIds = actionConfigDao.insertAll(entities)
            actions.forEachIndexed { i, a -> oldIdToNew[a.id] = newIds[i] }

            // Pass 2: update parentActionId for nested actions
            actions.forEachIndexed { i, a ->
                if (a.parentActionId != null) {
                    val newParentId = oldIdToNew[a.parentActionId]
                    if (newParentId != null) {
                        val entity = actionConfigDao.getById(newIds[i])
                        if (entity != null) {
                            actionConfigDao.update(entity.copy(parentActionId = newParentId))
                        }
                    }
                }
            }
        }

        return blockId
    }
}
