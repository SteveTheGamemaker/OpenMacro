package com.openmacro.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TypeRegistryTest {

    @Test
    fun `TriggerType fromTypeId resolves known types`() {
        assertEquals(TriggerType.SCREEN_ON_OFF, TriggerType.fromTypeId("screen_on_off"))
        assertEquals(TriggerType.BATTERY_LEVEL, TriggerType.fromTypeId("battery_level"))
        assertEquals(TriggerType.DAY_TIME, TriggerType.fromTypeId("day_time"))
    }

    @Test
    fun `TriggerType fromTypeId returns null for unknown type`() {
        assertNull(TriggerType.fromTypeId("nonexistent_type"))
    }

    @Test
    fun `ActionType fromTypeId resolves known types`() {
        assertEquals(ActionType.DISPLAY_NOTIFICATION, ActionType.fromTypeId("display_notification"))
        assertEquals(ActionType.VIBRATE, ActionType.fromTypeId("vibrate"))
        assertEquals(ActionType.SET_VARIABLE, ActionType.fromTypeId("set_variable"))
    }

    @Test
    fun `ActionType fromTypeId returns null for unknown type`() {
        assertNull(ActionType.fromTypeId("nonexistent_type"))
    }

    @Test
    fun `ConstraintType fromTypeId resolves known types`() {
        assertEquals(ConstraintType.BATTERY_LEVEL, ConstraintType.fromTypeId("battery_level"))
        assertEquals(ConstraintType.TIME_OF_DAY, ConstraintType.fromTypeId("time_of_day"))
        assertEquals(ConstraintType.VARIABLE_VALUE, ConstraintType.fromTypeId("variable_value"))
    }

    @Test
    fun `ConstraintType fromTypeId returns null for unknown type`() {
        assertNull(ConstraintType.fromTypeId("nonexistent_type"))
    }

    @Test
    fun `all TriggerType entries have unique typeIds`() {
        val typeIds = TriggerType.entries.map { it.typeId }
        assertEquals(typeIds.size, typeIds.toSet().size)
    }

    @Test
    fun `all ActionType entries have unique typeIds`() {
        val typeIds = ActionType.entries.map { it.typeId }
        assertEquals(typeIds.size, typeIds.toSet().size)
    }

    @Test
    fun `all ConstraintType entries have unique typeIds`() {
        val typeIds = ConstraintType.entries.map { it.typeId }
        assertEquals(typeIds.size, typeIds.toSet().size)
    }

    @Test
    fun `TriggerConfig type property resolves correctly`() {
        val config = TriggerConfig(macroId = 1, typeId = "screen_on_off")
        assertEquals(TriggerType.SCREEN_ON_OFF, config.type)
    }

    @Test
    fun `TriggerConfig type returns null for unknown typeId`() {
        val config = TriggerConfig(macroId = 1, typeId = "future_trigger")
        assertNull(config.type)
    }
}
