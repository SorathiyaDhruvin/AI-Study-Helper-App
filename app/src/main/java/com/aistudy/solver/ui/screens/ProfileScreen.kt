package com.aistudy.solver.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.*
import com.aistudy.solver.ui.viewmodel.AuthState
import com.aistudy.solver.ui.viewmodel.AuthViewModel
import com.aistudy.solver.utils.Constants
import com.aistudy.solver.utils.FileUtils
import com.aistudy.solver.utils.PremiumManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val scrollState = rememberScrollState()
    val coins by authViewModel.coins.collectAsState()
    val userData by authViewModel.userData.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val selectedImageUri by authViewModel.selectedImageUri.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val premiumManager = remember { PremiumManager(context) }
    val isPremium = remember { mutableStateOf(premiumManager.isPremium()) }

    var editMode by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var showSourcePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(userData) {
        userData?.let {
            newUsername = it.username
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        authViewModel.setSelectedImageUri(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            authViewModel.setSelectedImageUri(tempCameraUri)
        }
    }

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
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("My Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardDark, RoundedCornerShape(12.dp))
                        .clickable { editMode = !editMode },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (editMode) Icons.Filled.Close else Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            // 1. USER INFO SECTION (TOP)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, GradientStart, CircleShape)
                            .background(CardDark)
                            .clickable(enabled = editMode) {
                                showSourcePicker = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageSource = selectedImageUri ?: userData?.profileImage ?: Constants.DEFAULT_PROFILE_IMAGE
                        GlideImage(
                            model = imageSource,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (editMode) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (editMode) {
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                unfocusedBorderColor = OutlinedDark
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                authViewModel.updateProfile(
                                    username = newUsername,
                                    imageUri = selectedImageUri,
                                    currentImageUrl = userData?.profileImage ?: ""
                                )
                                editMode = false
                                authViewModel.setSelectedImageUri(null)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Profile updated") }
                            },
                            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GradientStart)
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(userData?.username ?: "User", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text(userData?.email ?: "Guest", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // 2. SUBSCRIPTION STATUS
                SectionTitle("SUBSCRIPTION")
                if (isPremium.value) {
                    SubscriptionCard(
                        title = "Premium Member",
                        subtitle = "Active Plan: AI Unlimited",
                        icon = Icons.Outlined.WorkspacePremium,
                        color = AccentGold,
                        actionText = "Manage"
                    ) { navController.navigate(Screen.Premium.route) }
                } else {
                    SubscriptionCard(
                        title = "Free User",
                        subtitle = "Limited AI Answers",
                        icon = Icons.Outlined.StarOutline,
                        color = TextSecondary,
                        actionText = "Upgrade to Premium"
                    ) { navController.navigate(Screen.Premium.route) }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // 3. USAGE STATS
                SectionTitle("USAGE STATS")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "42", "Solved", Icons.Filled.CheckCircle)
                    StatCard(Modifier.weight(1f), "12", "Docs", Icons.Filled.Description)
                    StatCard(Modifier.weight(1f), "5", "Daily", Icons.Filled.LocalFireDepartment)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. SETTINGS
                SectionTitle("PREFERENCES")
                CardGroup {
                    ActionRowItem(Icons.Outlined.Settings, "Settings", "Controls, themes, and account") {
                        navController.navigate(Screen.Settings.route)
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
        // Source Picker Dialog
    if (showSourcePicker) {
        AlertDialog(
            onDismissRequest = { showSourcePicker = false },
            containerColor = CardDark,
            title = { Text("Select Image Source", color = Color.White) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Camera", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showSourcePicker = false
                            val uri = FileUtils.createImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Gallery", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showSourcePicker = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourcePicker = false }) {
                    Text("Cancel", color = GradientEnd)
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
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
fun CardGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(16.dp))
            .border(1.dp, OutlinedDark, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun ActionRowItem(
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
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SubscriptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(16.dp))
            .border(1.dp, OutlinedDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
        Text(actionText, color = GradientStart, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .background(CardDark, RoundedCornerShape(16.dp))
            .border(1.dp, OutlinedDark, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = GradientStart, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

