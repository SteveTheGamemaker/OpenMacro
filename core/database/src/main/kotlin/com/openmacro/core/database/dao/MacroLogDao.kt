package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.openmacro.core.database.entity.MacroLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroLogDao {

    @Query("SELECT * FROM macro_logs ORDER BY started_at DESC")
    fun observeAll(): Flow<List<MacroLogEntity>>

    @Query("SELECT * FROM macro_logs WHERE macro_id = :macroId ORDER BY started_at DESC")
    fun observeByMacro(macroId: Long): Flow<List<MacroLogEntity>>

    @Query("SELECT * FROM macro_logs ORDER BY started_at DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<MacroLogEntity>>

    @Insert
    suspend fun insert(log: MacroLogEntity): Long

    @Query("UPDATE macro_logs SET completed_at = :completedAt, status = :status, error_message = :errorMessage WHERE id = :id")
    suspend fun updateCompletion(id: Long, completedAt: Long, status: String, errorMessage: String? = null)

    @Query("DELETE FROM macro_logs WHERE started_at < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM macro_logs")
    suspend fun deleteAll()
}
