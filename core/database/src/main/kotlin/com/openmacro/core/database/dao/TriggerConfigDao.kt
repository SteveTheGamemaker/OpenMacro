package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openmacro.core.database.entity.TriggerConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerConfigDao {

    @Query("SELECT * FROM trigger_configs WHERE macro_id = :macroId ORDER BY sort_order ASC")
    fun observeByMacro(macroId: Long): Flow<List<TriggerConfigEntity>>

    @Query("SELECT * FROM trigger_configs WHERE id = :id")
    suspend fun getById(id: Long): TriggerConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trigger: TriggerConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(triggers: List<TriggerConfigEntity>)

    @Update
    suspend fun update(trigger: TriggerConfigEntity)

    @Delete
    suspend fun delete(trigger: TriggerConfigEntity)

    @Query("DELETE FROM trigger_configs WHERE macro_id = :macroId")
    suspend fun deleteByMacro(macroId: Long)
}
