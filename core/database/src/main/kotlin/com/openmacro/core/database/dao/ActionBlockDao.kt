package com.openmacro.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openmacro.core.database.entity.ActionBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionBlockDao {

    @Query("SELECT * FROM action_blocks ORDER BY name ASC")
    fun observeAll(): Flow<List<ActionBlockEntity>>

    @Query("SELECT * FROM action_blocks WHERE id = :id")
    suspend fun getById(id: Long): ActionBlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: ActionBlockEntity): Long

    @Update
    suspend fun update(block: ActionBlockEntity)

    @Delete
    suspend fun delete(block: ActionBlockEntity)

    @Query("DELETE FROM action_blocks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
