package com.openmacro.core.database.repository

import com.openmacro.core.database.dao.VariableDao
import com.openmacro.core.database.entity.toDomain
import com.openmacro.core.database.entity.toEntity
import com.openmacro.core.model.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariableRepository @Inject constructor(
    private val variableDao: VariableDao,
) {
    fun observeAll(): Flow<List<Variable>> =
        variableDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeGlobals(): Flow<List<Variable>> =
        variableDao.observeGlobals().map { list -> list.map { it.toDomain() } }

    suspend fun getByName(name: String): Variable? =
        variableDao.getByName(name)?.toDomain()

    suspend fun upsert(variable: Variable): Long =
        variableDao.insert(variable.toEntity())

    suspend fun delete(variable: Variable) =
        variableDao.delete(variable.toEntity())

    suspend fun deleteByName(name: String) =
        variableDao.deleteByName(name)

    suspend fun clearGlobals() =
        variableDao.deleteAllGlobals()

    suspend fun clearAll() =
        variableDao.deleteAll()
}
