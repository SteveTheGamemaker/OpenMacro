package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openmacro.core.database.entity.ConstraintConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConstraintConfigDao {

    @Query("SELECT * FROM constraint_configs WHERE macro_id = :macroId ORDER BY sort_order ASC")
    fun observeByMacro(macroId: Long): Flow<List<ConstraintConfigEntity>>

    @Query("SELECT * FROM constraint_configs WHERE id = :id")
    suspend fun getById(id: Long): ConstraintConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(constraint: ConstraintConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(constraints: List<ConstraintConfigEntity>)

    @Update
    suspend fun update(constraint: ConstraintConfigEntity)

    @Delete
    suspend fun delete(constraint: ConstraintConfigEntity)

    @Query("DELETE FROM constraint_configs WHERE macro_id = :macroId")
    suspend fun deleteByMacro(macroId: Long)
}
