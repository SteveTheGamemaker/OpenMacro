package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trigger_configs",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macro_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("macro_id")],
)
data class TriggerConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "macro_id")
    val macroId: Long,
    val type: String,
    @ColumnInfo(name = "config_json")
    val configJson: String = "{}",
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
