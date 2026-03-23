package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openmacro.core.database.entity.ActionConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionConfigDao {

    @Query("SELECT * FROM action_configs WHERE macro_id = :macroId ORDER BY sort_order ASC")
    fun observeByMacro(macroId: Long): Flow<List<ActionConfigEntity>>

    @Query("SELECT * FROM action_configs WHERE id = :id")
    suspend fun getById(id: Long): ActionConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ActionConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<ActionConfigEntity>)

    @Update
    suspend fun update(action: ActionConfigEntity)

    @Delete
    suspend fun delete(action: ActionConfigEntity)

    @Query("DELETE FROM action_configs WHERE macro_id = :macroId")
    suspend fun deleteByMacro(macroId: Long)
}
