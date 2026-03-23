package com.openmacro.core.database.di

import android.content.Context
import androidx.room.Room
import com.openmacro.core.database.OpenMacroDatabase
import com.openmacro.core.database.dao.ActionConfigDao
import com.openmacro.core.database.dao.CategoryDao
import com.openmacro.core.database.dao.ConstraintConfigDao
import com.openmacro.core.database.dao.MacroDao
import com.openmacro.core.database.dao.MacroLogDao
import com.openmacro.core.database.dao.TriggerConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        ).build()

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
}
