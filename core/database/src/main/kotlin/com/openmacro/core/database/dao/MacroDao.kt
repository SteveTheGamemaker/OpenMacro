package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.openmacro.core.database.entity.MacroEntity
import com.openmacro.core.database.relation.MacroWithDetailsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {

    @Query("SELECT * FROM macros ORDER BY sort_order ASC, name ASC")
    fun observeAll(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE is_enabled = 1 ORDER BY sort_order ASC")
    fun observeEnabled(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE category_id = :categoryId ORDER BY sort_order ASC")
    fun observeByCategory(categoryId: Long): Flow<List<MacroEntity>>

    @Transaction
    @Query("SELECT * FROM macros ORDER BY sort_order ASC, name ASC")
    fun observeAllWithDetails(): Flow<List<MacroWithDetailsRelation>>

    @Transaction
    @Query("SELECT * FROM macros WHERE id = :macroId")
    fun observeWithDetails(macroId: Long): Flow<MacroWithDetailsRelation?>

    @Transaction
    @Query("SELECT * FROM macros WHERE is_enabled = 1")
    fun observeEnabledWithDetails(): Flow<List<MacroWithDetailsRelation>>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getById(id: Long): MacroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(macro: MacroEntity): Long

    @Update
    suspend fun update(macro: MacroEntity)

    @Delete
    suspend fun delete(macro: MacroEntity)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE macros SET is_enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}
