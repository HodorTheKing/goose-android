package com.block.goose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.goose.data.api.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connection Section
            SettingsSection(title = "Connection") {
                // Base URL
                OutlinedTextField(
                    value = uiState.baseUrl,
                    onValueChange = { newUrl ->
                        // Update via ViewModel
                    },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://demo-goosed.fly.dev") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Secret Key
                OutlinedTextField(
                    value = uiState.secretKey,
                    onValueChange = { },
                    label = { Text("Secret Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Connection Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (uiState.connectionStatus) {
                                ConnectionStatus.CONNECTED -> "Connected"
                                ConnectionStatus.CONNECTING -> "Connecting..."
                                ConnectionStatus.FAILED -> "Connection Failed"
                                else -> "Not Tested"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        uiState.error?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.testConnection() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test")
                        }
                    }
                }
            }
            
            // Appearance Section
            SettingsSection(title = "Appearance") {
                // Theme Mode
                ListItem(
                    headlineContent = { Text("Theme") },
                    leadingContent = { Icon(Icons.Default.Brush, null) },
                    trailingContent = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(uiState.themeMode.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                UserSettings.ThemeMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            viewModel.setThemeMode(mode)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
                
                // Text Size
                ListItem(
                    headlineContent = { Text("Text Size") },
                    leadingContent = { Icon(Icons.Default.TextFormat, null) },
                    trailingContent = {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(uiState.textSize.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                UserSettings.TextSize.values().forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text(size.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            viewModel.setTextSize(size)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
            
            // Security Section
            SettingsSection(title = "Security") {
                ListItem(
                    headlineContent = { Text("Certificate Pinning") },
                    supportingContent = { Text("Pin certificates for extra security") },
                    leadingContent = { Icon(Icons.Default.Security, null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.certificatePinning,
                            onCheckedChange = { viewModel.setCertificatePinning(it) }
                        )
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Biometric Authentication") },
                    supportingContent = { Text("Require fingerprint/face unlock") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                    }
                )
            }
            
            // Data Management Section
            SettingsSection(title = "Data Management") {
                ListItem(
                    headlineContent = { Text("Export Chat History") },
                    supportingContent = { Text("Export conversations to JSON") },
                    leadingContent = { Icon(Icons.Default.Download, null) },
                    modifier = Modifier.clickable { showExportDialog = true }
                )
                
                ListItem(
                    headlineContent = { Text("Clear Local Data") },
                    supportingContent = { Text("Remove all locally stored data") },
                    leadingContent = { 
                        Icon(
                            Icons.Default.Delete, 
                            null,
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    modifier = Modifier.clickable { showClearDataDialog = true }
                )
            }
            
            // About Section
            SettingsSection(title = "About") {
                ListItem(
                    headlineContent = { Text("Goose Companion") },
                    supportingContent = { Text("v1.1.0") },
                    leadingContent = { Icon(Icons.Default.Settings, null) }
                )
            }
            
            // Reset to Trial
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Warning, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset to Trial Mode")
            }
        }
    }
    
    // Dialogs
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Trial Mode?") },
            text = { Text("This will reset your server configuration to use the demo server.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToTrialMode()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Local Data?") },
            text = { Text("This will delete all chat history and settings stored locally on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLocalData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 16.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(content = content)
        }
    }
}
