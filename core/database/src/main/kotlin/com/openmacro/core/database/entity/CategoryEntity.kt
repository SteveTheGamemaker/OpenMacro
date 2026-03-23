package com.openmacro.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    @ColumnInfo(name = "icon_name")
    val iconName: String = "folder",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
