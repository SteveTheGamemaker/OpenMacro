package com.openmacro.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

private val DAYS = listOf(
    Calendar.MONDAY to "M",
    Calendar.TUESDAY to "T",
    Calendar.WEDNESDAY to "W",
    Calendar.THURSDAY to "T",
    Calendar.FRIDAY to "F",
    Calendar.SATURDAY to "S",
    Calendar.SUNDAY to "S",
)

@Composable
fun DayOfWeekSelector(
    selectedDays: List<Int>,
    onDaysChanged: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DAYS.forEach { (dayConstant, label) ->
            val selected = dayConstant in selectedDays
            FilterChip(
                selected = selected,
                onClick = {
                    onDaysChanged(
                        if (selected) selectedDays - dayConstant
                        else selectedDays + dayConstant,
                    )
                },
                label = { Text(label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
