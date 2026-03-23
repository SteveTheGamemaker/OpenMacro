package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "macro_logs",
    indices = [Index("macro_id")],
)
data class MacroLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "macro_id")
    val macroId: Long,
    @ColumnInfo(name = "macro_name")
    val macroName: String,
    @ColumnInfo(name = "trigger_type")
    val triggerType: String,
    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    val status: String = "SUCCESS",
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
)
