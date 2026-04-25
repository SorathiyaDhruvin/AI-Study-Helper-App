package com.aistudy.solver.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aistudy.solver.data.model.ChatMessage
import com.aistudy.solver.data.model.ChatSession
import com.aistudy.solver.ui.theme.*
import com.aistudy.solver.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, chatViewModel: ChatViewModel = viewModel()) {
    var inputMessage by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    val isTyping by chatViewModel.isTyping.collectAsState()
    val currentSession by chatViewModel.currentSession.collectAsState()
    val chatSessions by chatViewModel.chatSessions.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Menu states
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    // Voice input
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) inputMessage = spoken
        }
    }

    // Camera
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Image captured! Sending...", Toast.LENGTH_SHORT).show()
            chatViewModel.sendMessage("[Image from camera]", type = "image")
        }
    }
    val cameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    // Gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
            chatViewModel.sendMessage("[Image from gallery]", type = "image", mediaUrl = uri.toString())
        }
    }

    // File picker
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "File attached!", Toast.LENGTH_SHORT).show()
            chatViewModel.sendMessage("[File attached]", type = "file", mediaUrl = uri.toString())
        }
    }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Sidebar drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            ChatSidebar(
                sessions = chatSessions,
                currentSessionId = currentSession?.id,
                onNewChat = {
                    chatViewModel.startNewChat()
                    scope.launch { drawerState.close() }
                },
                onSelectChat = { session ->
                    chatViewModel.openChat(session.id)
                    scope.launch { drawerState.close() }
                },
                onDeleteChat = { session ->
                    chatViewModel.deleteChat(session.id)
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(BackgroundDark)
        ) {
            // ─── Top Bar ─────────────────────────────
            Surface(color = SurfaceDark, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, "Sidebar", tint = TextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            currentSession?.title ?: "New Chat",
                            color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(SuccessColor, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("Gemini AI", color = SuccessColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    IconButton(onClick = {
                        chatViewModel.startNewChat()
                    }) {
                        Icon(Icons.Filled.Edit, "New Chat", tint = TextSecondary)
                    }
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(Icons.Filled.MoreVert, "Options", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            containerColor = CardDark
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename chat", color = TextPrimary) },
                                onClick = {
                                    showOptionsMenu = false
                                    renameText = currentSession?.title ?: ""
                                    showRenameDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.DriveFileRenameOutline, null, tint = TextSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear conversation", color = TextPrimary) },
                                onClick = {
                                    showOptionsMenu = false
                                    chatViewModel.clearConversation()
                                },
                                leadingIcon = { Icon(Icons.Filled.CleaningServices, null, tint = TextSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share chat", color = TextPrimary) },
                                onClick = {
                                    showOptionsMenu = false
                                    val shareText = messages.joinToString("\n\n") { (if (it.isAi) "AI: " else "You: ") + it.text }
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        this.type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Chat"))
                                },
                                leadingIcon = { Icon(Icons.Filled.Share, null, tint = TextSecondary) }
                            )
                            HorizontalDivider(color = OutlinedDark)
                            DropdownMenuItem(
                                text = { Text("Delete chat", color = ErrorColor) },
                                onClick = {
                                    showOptionsMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = ErrorColor) }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = OutlinedDark, thickness = 0.5.dp)

            // ─── Chat Messages ─────────────────────────
            if (isLoading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GradientStart)
                }
            } else if (messages.isEmpty() && !isTyping) {
                // Empty state
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = GradientStart, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("How can I help you today?", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Ask any study question", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(32.dp))
                        // Suggestion chips
                        val suggestions = listOf("Explain quantum physics", "Solve: 2x + 5 = 15", "What is photosynthesis?")
                        suggestions.forEach { s ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 4.dp).clickable {
                                    inputMessage = s
                                },
                                color = CardDark, shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, OutlinedDark)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lightbulb, null, tint = AccentGold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(s, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(messages, key = { it.id.ifBlank { it.hashCode().toString() } }) { msg ->
                        ChatMessageBubble(msg, context, onRegenerate = { chatViewModel.regenerateLastResponse() })
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }

            // ─── Input Bar ─────────────────────────────
            Surface(color = SurfaceDark, tonalElevation = 4.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        // Plus button
                        IconButton(onClick = { showAttachSheet = true }, modifier = Modifier.size(44.dp)) {
                            Box(Modifier.size(32.dp).background(CardDark, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Add, "Attach", tint = TextPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        // Text field
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = { Text("Type your question...", color = TextSecondary, fontSize = 15.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = OutlinedDark,
                                focusedBorderColor = GradientStart,
                                unfocusedContainerColor = BackgroundDark,
                                focusedContainerColor = BackgroundDark,
                                unfocusedTextColor = TextPrimary,
                                focusedTextColor = TextPrimary,
                                cursorColor = GradientStart
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        // Mic / Send
                        if (inputMessage.isBlank()) {
                            IconButton(onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your question...")
                                }
                                try { voiceLauncher.launch(intent) }
                                catch (e: Exception) { Toast.makeText(context, "Voice input not available", Toast.LENGTH_SHORT).show() }
                            }, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.Filled.Mic, "Voice", tint = TextSecondary)
                            }
                        } else {
                            IconButton(onClick = {
                                chatViewModel.sendMessage(inputMessage.trim())
                                inputMessage = ""
                            }, modifier = Modifier.size(44.dp)) {
                                Box(
                                    Modifier.size(36.dp).background(
                                        Brush.linearGradient(listOf(GradientStart, GradientEnd)), CircleShape
                                    ), contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── Attachment Bottom Sheet ─────────────────────
    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            containerColor = CardDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Attach", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                AttachOption(Icons.Filled.CameraAlt, "Camera", "Take a photo") {
                    showAttachSheet = false
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) cameraLauncher.launch(null) else cameraPermLauncher.launch(Manifest.permission.CAMERA)
                }
                AttachOption(Icons.Filled.Image, "Gallery", "Pick from photos") {
                    showAttachSheet = false
                    galleryLauncher.launch("image/*")
                }
                AttachOption(Icons.Filled.InsertDriveFile, "Files", "PDF, DOC, etc.") {
                    showAttachSheet = false
                    fileLauncher.launch("*/*")
                }
                AttachOption(Icons.Filled.Screenshot, "Screenshot", "Capture screen") {
                    showAttachSheet = false
                    Toast.makeText(context, "Screenshot feature coming soon", Toast.LENGTH_SHORT).show()
                }
                AttachOption(Icons.Filled.Mic, "Voice", "Record audio") {
                    showAttachSheet = false
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    }
                    try { voiceLauncher.launch(intent) } catch (_: Exception) {}
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ─── Rename Dialog ──────────────────────────────────
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = CardDark,
            title = { Text("Rename Chat", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText, onValueChange = { renameText = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlinedDark, focusedBorderColor = GradientStart,
                        unfocusedTextColor = TextPrimary, focusedTextColor = TextPrimary, cursorColor = GradientStart
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    currentSession?.id?.let { chatViewModel.renameChat(it, renameText) }
                    showRenameDialog = false
                }) { Text("Save", color = GradientStart) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    // ─── Delete Dialog ──────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardDark,
            title = { Text("Delete Chat?", color = TextPrimary) },
            text = { Text("This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    currentSession?.id?.let { chatViewModel.deleteChat(it) }
                    showDeleteDialog = false
                }) { Text("Delete", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

// ─── Sidebar ──────────────────────────────────────────────
@Composable
fun ChatSidebar(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onNewChat: () -> Unit,
    onSelectChat: (ChatSession) -> Unit,
    onDeleteChat: (ChatSession) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = SurfaceDark,
        modifier = Modifier.width(300.dp)
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chat History", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Close", tint = TextSecondary)
                }
            }

            // New Chat button
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onNewChat() },
                color = GradientStart.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GradientStart.copy(alpha = 0.3f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, null, tint = GradientStart, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("New Chat", color = GradientStart, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = OutlinedDark, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            if (sessions.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Forum, null, tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No chats yet", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        var showItemMenu by remember { mutableStateOf(false) }
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                .clickable { onSelectChat(session) },
                            color = if (isSelected) GradientStart.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ChatBubbleOutline, null, tint = if (isSelected) GradientStart else TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    session.title, color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                                )
                                Box {
                                    IconButton(onClick = { showItemMenu = true }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }, containerColor = CardDark) {
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = ErrorColor, fontSize = 13.sp) },
                                            onClick = { showItemMenu = false; onDeleteChat(session) },
                                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = ErrorColor, modifier = Modifier.size(16.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Chat Bubble ───────────────────────────────────────
@Composable
fun ChatMessageBubble(message: ChatMessage, context: Context, onRegenerate: () -> Unit) {
    if (message.isAi) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
                Box(Modifier.size(32.dp).background(GradientStart.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = GradientStart, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(message.text, color = TextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
                }
            }
            // Action row
            Row(Modifier.padding(start = 42.dp, top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MsgActionButton(Icons.Outlined.ContentCopy, "Copy") {
                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.setPrimaryClip(ClipData.newPlainText("AI Response", message.text))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                }
                MsgActionButton(Icons.Outlined.Refresh, "Regenerate") { onRegenerate() }
                MsgActionButton(Icons.Outlined.ThumbUp, "Like") {
                    Toast.makeText(context, "Thanks for the feedback!", Toast.LENGTH_SHORT).show()
                }
                MsgActionButton(Icons.Outlined.ThumbDown, "Dislike") {
                    Toast.makeText(context, "We'll improve!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier.widthIn(max = 300.dp)
                    .background(GradientStart, RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))
                    .padding(14.dp)
            ) {
                Text(message.text, color = Color.White, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun MsgActionButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(icon, desc, tint = TextSecondary, modifier = Modifier.size(15.dp))
    }
}

// ─── Typing Indicator ──────────────────────────────────
@Composable
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(GradientStart.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, null, tint = GradientStart, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = i * 200), RepeatMode.Reverse),
                    label = "dot$i"
                )
                Box(Modifier.size(8.dp).background(GradientStart.copy(alpha = alpha), CircleShape))
            }
        }
    }
}

// ─── Attach Option ──────────────────────────────────────
@Composable
fun AttachOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        color = Color.Transparent, shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(GradientStart.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = GradientStart, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
