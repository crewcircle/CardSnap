package com.cardscannerapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cardscannerapp.data.db.ContactDatabase
import com.cardscannerapp.data.repository.ContactRepository
import com.cardscannerapp.data.repository.SettingsRepository
import com.cardscannerapp.domain.model.DataUsagePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(SettingsRepository(context), ContactRepository(ContactDatabase.getInstance(context).contactDao())) }
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } })
    }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Scanner", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            var showLangDialog by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth().clickable { showLangDialog = true }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("OCR Languages"); Text(uiState.settings.ocrLanguages.joinToString(", "), color = Color.Gray)
            }
            if (showLangDialog) {
                val languages = listOf("eng" to "English", "chi_sim" to "Chinese", "deu" to "German", "fra" to "French", "spa" to "Spanish")
                AlertDialog(onDismissRequest = { showLangDialog = false }, title = { Text("Select OCR Languages") },
                    text = { Column { languages.forEach { (code, label) ->
                        val selected = uiState.settings.ocrLanguages.contains(code)
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            val current = uiState.settings.ocrLanguages.toMutableList()
                            if (selected) current.remove(code) else current.add(code)
                            viewModel.updateSettings(uiState.settings.copy(ocrLanguages = current))
                        }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selected, onCheckedChange = null); Spacer(modifier = Modifier.width(8.dp)); Text(label)
                        }
                    } } }, confirmButton = { TextButton(onClick = { showLangDialog = false }) { Text("Done") } })
            }
            Divider(); SettingSwitch("Auto-save contacts", uiState.settings.autoSave) { viewModel.updateSettings(uiState.settings.copy(autoSave = it)) }
            Divider(); SettingSwitch("Notifications", uiState.settings.notifications) { viewModel.updateSettings(uiState.settings.copy(notifications = it)) }
            Divider(); SettingSwitch("Haptic feedback", uiState.settings.hapticEnabled) { viewModel.updateSettings(uiState.settings.copy(hapticEnabled = it)) }
            Divider()
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Data usage"); Text(if (uiState.settings.dataUsage == DataUsagePreference.WIFI_ONLY) "Wi-Fi only" else "Cellular allowed", color = Color.Gray)
            }
            Divider(); Spacer(modifier = Modifier.height(24.dp))
            var showResetDialog by remember { mutableStateOf(false) }
            Button(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Reset All Data")
            }
            if (showResetDialog) {
                AlertDialog(onDismissRequest = { showResetDialog = false }, title = { Text("Reset All Data") },
                    text = { Text("This will delete all contacts and settings. This cannot be undone.") },
                    dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } },
                    confirmButton = { TextButton(onClick = { viewModel.resetAllData(); showResetDialog = false }) { Text("Reset", color = Color.Red) } })
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title); Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
