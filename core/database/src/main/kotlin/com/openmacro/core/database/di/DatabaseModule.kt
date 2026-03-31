package com.openmacro.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.openmacro.core.database.OpenMacroDatabase
import com.openmacro.core.database.dao.ActionBlockDao
import com.openmacro.core.database.dao.ActionConfigDao
import com.openmacro.core.database.dao.CategoryDao
import com.openmacro.core.database.dao.ConstraintConfigDao
import com.openmacro.core.database.dao.MacroDao
import com.openmacro.core.database.dao.MacroLogDao
import com.openmacro.core.database.dao.TriggerConfigDao
import com.openmacro.core.database.dao.VariableDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `action_blocks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '',
                `input_params_json` TEXT NOT NULL DEFAULT '[]',
                `output_params_json` TEXT NOT NULL DEFAULT '[]',
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `variables` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL DEFAULT 'STRING',
                `value_json` TEXT NOT NULL DEFAULT '""',
                `is_global` INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_variables_name` ON `variables` (`name`)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenMacroDatabase =
        Room.databaseBuilder(
            context,
            OpenMacroDatabase::class.java,
            "openmacro.db",
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideMacroDao(db: OpenMacroDatabase): MacroDao = db.macroDao()

    @Provides
    fun provideCategoryDao(db: OpenMacroDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTriggerConfigDao(db: OpenMacroDatabase): TriggerConfigDao = db.triggerConfigDao()

    @Provides
    fun provideActionConfigDao(db: OpenMacroDatabase): ActionConfigDao = db.actionConfigDao()

    @Provides
    fun provideConstraintConfigDao(db: OpenMacroDatabase): ConstraintConfigDao = db.constraintConfigDao()

    @Provides
    fun provideMacroLogDao(db: OpenMacroDatabase): MacroLogDao = db.macroLogDao()

    @Provides
    fun provideVariableDao(db: OpenMacroDatabase): VariableDao = db.variableDao()

    @Provides
    fun provideActionBlockDao(db: OpenMacroDatabase): ActionBlockDao = db.actionBlockDao()
}
