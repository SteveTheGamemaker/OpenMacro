package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "constraint_configs",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macro_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("macro_id"), Index("parent_constraint_id")],
)
data class ConstraintConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "macro_id")
    val macroId: Long,
    @ColumnInfo(name = "parent_constraint_id")
    val parentConstraintId: Long? = null,
    val type: String,
    @ColumnInfo(name = "config_json")
    val configJson: String = "{}",
    @ColumnInfo(name = "logic_operator")
    val logicOperator: String = "AND",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
