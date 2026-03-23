package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "action_configs",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macro_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("macro_id"), Index("parent_action_id")],
)
data class ActionConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "macro_id")
    val macroId: Long,
    @ColumnInfo(name = "action_block_id")
    val actionBlockId: Long? = null,
    val type: String,
    @ColumnInfo(name = "config_json")
    val configJson: String = "{}",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "parent_action_id")
    val parentActionId: Long? = null,
)
