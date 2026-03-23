package com.openmacro.feature.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openmacro.core.model.MacroLog
import com.openmacro.core.model.MacroLogStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: LogsViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                actions = {
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear logs")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (logs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No logs yet",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Macro execution logs will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs, key = { it.id }) { log ->
                    LogItem(log)
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: MacroLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon: androidx.compose.ui.graphics.vector.ImageVector
            val tint: Color
            when (log.status) {
                MacroLogStatus.SUCCESS -> {
                    icon = Icons.Default.CheckCircle
                    tint = Color(0xFF4CAF50)
                }
                MacroLogStatus.FAILURE -> {
                    icon = Icons.Default.CheckCircle // reuse icon, different color
                    tint = MaterialTheme.colorScheme.error
                }
                MacroLogStatus.CANCELLED -> {
                    icon = Icons.Default.CheckCircle
                    tint = MaterialTheme.colorScheme.outline
                }
            }
            Icon(
                imageVector = icon,
                contentDescription = log.status.name,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.macroName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "Trigger: ${log.triggerType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                log.errorMessage?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTime(log.startedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
private val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return if (diff < 24 * 60 * 60 * 1000) {
        timeFormat.format(Date(timestamp))
    } else {
        dateFormat.format(Date(timestamp))
    }
}
