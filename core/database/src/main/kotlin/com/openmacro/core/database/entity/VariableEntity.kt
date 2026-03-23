package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "variables",
    indices = [Index("name", unique = true)],
)
data class VariableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "STRING",
    @ColumnInfo(name = "value_json")
    val valueJson: String = "\"\"",
    @ColumnInfo(name = "is_global")
    val isGlobal: Boolean = true,
)
