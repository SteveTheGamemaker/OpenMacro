package com.openmacro.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openmacro.core.database.dao.ActionConfigDao
import com.openmacro.core.database.dao.CategoryDao
import com.openmacro.core.database.dao.ConstraintConfigDao
import com.openmacro.core.database.dao.MacroDao
import com.openmacro.core.database.dao.MacroLogDao
import com.openmacro.core.database.dao.TriggerConfigDao
import com.openmacro.core.database.entity.ActionConfigEntity
import com.openmacro.core.database.entity.CategoryEntity
import com.openmacro.core.database.entity.ConstraintConfigEntity
import com.openmacro.core.database.entity.MacroEntity
import com.openmacro.core.database.entity.MacroLogEntity
import com.openmacro.core.database.entity.TriggerConfigEntity

@Database(
    entities = [
        MacroEntity::class,
        CategoryEntity::class,
        TriggerConfigEntity::class,
        ActionConfigEntity::class,
        ConstraintConfigEntity::class,
        MacroLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OpenMacroDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun categoryDao(): CategoryDao
    abstract fun triggerConfigDao(): TriggerConfigDao
    abstract fun actionConfigDao(): ActionConfigDao
    abstract fun constraintConfigDao(): ConstraintConfigDao
    abstract fun macroLogDao(): MacroLogDao
}
