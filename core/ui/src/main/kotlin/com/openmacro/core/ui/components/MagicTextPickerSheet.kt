package com.openmacro.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

data class MagicTextToken(
    val token: String,
    val description: String,
    val category: String,
)

/**
 * Bottom sheet that shows categorized magic text tokens for insertion.
 * Ordering: trigger-specific tokens first, then user variables, then built-ins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicTextPickerSheet(
    triggerTypeIds: List<String> = emptyList(),
    userVariables: List<Pair<String, String>> = emptyList(),
    onTokenSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = buildTokenList(triggerTypeIds, userVariables)
    val grouped = tokens.groupBy { it.category }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "Insert Magic Text",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            LazyColumn {
                grouped.forEach { (category, categoryTokens) ->
                    item {
                        Text(
                            category,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(categoryTokens) { token ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTokenSelected("{${token.token}}") }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                        ) {
                            Text(
                                "{${token.token}}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                token.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

private fun buildTokenList(
    triggerTypeIds: List<String>,
    userVariables: List<Pair<String, String>>,
): List<MagicTextToken> {
    val tokens = mutableListOf<MagicTextToken>()
    val addedTokens = mutableSetOf<String>()

    // 1. Trigger-specific tokens (shown first, one section per trigger type)
    for (typeId in triggerTypeIds) {
        val triggerTokens = triggerTokensFor(typeId)
        for (t in triggerTokens) {
            if (addedTokens.add(t.token)) tokens.add(t)
        }
    }

    // 2. User-defined variables
    if (userVariables.isNotEmpty()) {
        userVariables.forEach { (name, type) ->
            val key = "v_$name"
            if (addedTokens.add(key)) {
                tokens.add(MagicTextToken(key, "$type variable", "Your Variables"))
            }
        }
    }

    // 3. Built-in tokens
    tokens.addAll(
        listOf(
            MagicTextToken("battery_level", "Current battery %", "Device"),
            MagicTextToken("device_name", "Device model name", "Device"),
            MagicTextToken("time", "Current time (HH:mm)", "Date & Time"),
            MagicTextToken("date", "Current date (yyyy-MM-dd)", "Date & Time"),
            MagicTextToken("ssid", "Current WiFi name", "Network"),
            MagicTextToken("macro_name", "Name of this macro", "Macro"),
            MagicTextToken("trigger_type", "Trigger type that fired", "Macro"),
            MagicTextToken("lv_repeat_index", "Current loop index", "Flow Control"),
            MagicTextToken("lv_key", "Current iteration key", "Flow Control"),
            MagicTextToken("lv_value", "Current iteration value", "Flow Control"),
            MagicTextToken("http_response_body", "Last HTTP response", "Network"),
            MagicTextToken("http_response_code", "Last HTTP status code", "Network"),
        ),
    )

    return tokens
}

private fun triggerTokensFor(typeId: String): List<MagicTextToken> = when (typeId) {
    "sms_received" -> listOf(
        MagicTextToken("sms_sender", "Sender phone number", "SMS Trigger"),
        MagicTextToken("sms_message", "Message content", "SMS Trigger"),
    )
    "sms_sent" -> listOf(
        MagicTextToken("sms_recipient", "Recipient phone number", "SMS Sent Trigger"),
        MagicTextToken("sms_message", "Message content", "SMS Sent Trigger"),
    )
    "call_incoming" -> listOf(
        MagicTextToken("call_number", "Caller phone number", "Call Trigger"),
    )
    "call_ended" -> listOf(
        MagicTextToken("call_number", "Call phone number", "Call Trigger"),
    )
    "call_missed" -> listOf(
        MagicTextToken("call_number", "Missed call number", "Call Trigger"),
    )
    "battery_level" -> listOf(
        MagicTextToken("trigger_battery_level", "Battery level that fired", "Battery Trigger"),
    )
    "regular_interval" -> listOf(
        MagicTextToken("trigger_interval_count", "Interval count", "Interval Trigger"),
    )
    else -> emptyList()
}
