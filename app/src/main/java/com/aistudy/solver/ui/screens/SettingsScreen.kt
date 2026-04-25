package com.aistudy.solver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.*
import com.aistudy.solver.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val themeMode = LocalThemeMode.current
    val isDarkMode = themeMode.value
    var pushNotifications by remember { mutableStateOf(true) }
    var dailyReminders by remember { mutableStateOf(false) }
    var studyTips by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var appSounds by remember { mutableStateOf(false) }
    var shortAnswerMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundDark,
        topBar = {
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(top = statusBarPadding + 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardDark, RoundedCornerShape(12.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(40.dp)) // For balance
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Settings
            SettingsHeader("APP SETTINGS")
            SettingsCard {
                SettingsSwitchItem(Icons.Outlined.DarkMode, "Dark Mode", isDarkMode, onCheckedChange = { themeMode.value = it })
                HorizontalDivider(color = OutlinedDark)
                SettingsSwitchItem(Icons.Outlined.ShortText, "Short Answer Mode", shortAnswerMode, onCheckedChange = { shortAnswerMode = it })
                HorizontalDivider(color = OutlinedDark)
                SettingsNavigationItem(Icons.Outlined.ColorLens, "App Theme", "System Default")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications
            SettingsHeader("NOTIFICATIONS")
            SettingsCard {
                SettingsSwitchItem(Icons.Outlined.Notifications, "Push Notifications", pushNotifications, onCheckedChange = { pushNotifications = it })
                HorizontalDivider(color = OutlinedDark)
                SettingsSwitchItem(Icons.Outlined.Alarm, "Daily Reminders", dailyReminders, onCheckedChange = { dailyReminders = it })
                HorizontalDivider(color = OutlinedDark)
                SettingsSwitchItem(Icons.Outlined.TipsAndUpdates, "Study Tips", studyTips, onCheckedChange = { studyTips = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sound & Vibration
            SettingsHeader("SOUND & VIBRATION")
            SettingsCard {
                SettingsSwitchItem(Icons.Outlined.Vibration, "Haptic Feedback", hapticFeedback, onCheckedChange = { hapticFeedback = it })
                HorizontalDivider(color = OutlinedDark)
                SettingsSwitchItem(Icons.Outlined.VolumeUp, "App Sounds", appSounds, onCheckedChange = { appSounds = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Data
            SettingsHeader("PRIVACY & DATA")
            SettingsCard {
                SettingsActionItem(Icons.Outlined.Download, "Export My Data") { 
                    coroutineScope.launch { snackbarHostState.showSnackbar("Exporting data...") }
                }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.Policy, "Privacy Policy") { }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.Description, "Terms & Conditions") { }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Document Options
            SettingsHeader("DOCUMENT OPTIONS")
            SettingsCard {
                SettingsActionItem(Icons.Outlined.Folder, "Manage Documents") { }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.DeleteSweep, "Clear All Documents", tint = Color.Red.copy(alpha=0.8f)) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Documents cleared") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History & Feedback
            SettingsHeader("HISTORY & FEEDBACK")
            SettingsCard {
                SettingsActionItem(Icons.Outlined.History, "View Query History") { }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.ClearAll, "Clear History", tint = Color.Red.copy(alpha=0.8f)) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("History cleared") }
                }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.Feedback, "Send Feedback") { }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language & Support
            SettingsHeader("LANGUAGE & SUPPORT")
            SettingsCard {
                SettingsNavigationItem(Icons.Outlined.Language, "Language", "English (US)")
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.Email, "Contact Support") { }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.StarRate, "Rate App") { }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.Share, "Share App") { }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Management
            SettingsHeader("ACCOUNT MANAGEMENT")
            SettingsCard {
                SettingsActionItem(Icons.Outlined.Restore, "Reset Data", subtitle = "Clear docs, history, cache") { showResetDialog = true }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.CloudDownload, "Backup & Restore", subtitle = "Export/Import JSON") {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Backup feature coming soon") }
                }
                HorizontalDivider(color = OutlinedDark)
                SettingsActionItem(Icons.Outlined.DeleteOutline, "Delete Account", subtitle = "Permanently delete all data", tint = Color.Red.copy(alpha=0.8f)) { showDeleteDialog = true }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, tint = Color.Red.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color.Red.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = CardDark,
                title = { Text("Delete Account?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("This action cannot be undone. All your data, documents, and history will be permanently deleted.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showDeleteDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Deleting account...")
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        // Reset Data Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = CardDark,
                title = { Text("Reset Data?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("This will clear all your documents, history, and cache. Your account will be kept.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showResetDialog = false
                            coroutineScope.launch { snackbarHostState.showSnackbar("Data reset successful") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                    ) {
                        Text("Reset", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(16.dp))
            .border(1.dp, OutlinedDark, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryColor,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BackgroundDark,
                uncheckedBorderColor = OutlinedDark
            )
        )
    }
}

@Composable
fun SettingsNavigationItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(text = value, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Filled.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}
