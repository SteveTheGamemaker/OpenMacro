package com.openmacro.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.openmacro.core.database.entity.ActionConfigEntity
import com.openmacro.core.database.entity.ConstraintConfigEntity
import com.openmacro.core.database.entity.MacroEntity
import com.openmacro.core.database.entity.TriggerConfigEntity

data class MacroWithDetailsRelation(
    @Embedded
    val macro: MacroEntity,

    @Relation(parentColumn = "id", entityColumn = "macro_id")
    val triggers: List<TriggerConfigEntity>,

    @Relation(parentColumn = "id", entityColumn = "macro_id")
    val actions: List<ActionConfigEntity>,

    @Relation(parentColumn = "id", entityColumn = "macro_id")
    val constraints: List<ConstraintConfigEntity>,
)
