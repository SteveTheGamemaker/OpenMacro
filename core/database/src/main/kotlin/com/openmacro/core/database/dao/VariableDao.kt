package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openmacro.core.database.entity.VariableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {

    @Query("SELECT * FROM variables ORDER BY name ASC")
    fun observeAll(): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE is_global = 1 ORDER BY name ASC")
    fun observeGlobals(): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variables WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): VariableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(variable: VariableEntity): Long

    @Delete
    suspend fun delete(variable: VariableEntity)

    @Query("DELETE FROM variables WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM variables WHERE is_global = 1")
    suspend fun deleteAllGlobals()

    @Query("DELETE FROM variables")
    suspend fun deleteAll()
}
