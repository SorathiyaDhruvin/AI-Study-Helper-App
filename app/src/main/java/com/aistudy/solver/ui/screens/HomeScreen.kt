package com.aistudy.solver.ui.screens

import android.app.Activity
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.aistudy.solver.data.model.ChatSession
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.*
import com.aistudy.solver.utils.BannerAdView
import com.aistudy.solver.utils.Constants
import com.aistudy.solver.utils.InterstitialAdManager
import com.aistudy.solver.utils.RewardedAdManager
import com.aistudy.solver.utils.PremiumManager
import com.aistudy.solver.ui.viewmodel.AuthViewModel
import com.aistudy.solver.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(navController: NavController, openDrawer: () -> Unit = {}) {
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val coins by authViewModel.coins.collectAsState()
    val userData by authViewModel.userData.collectAsState()
    val recentQuestions by chatViewModel.recentQuestions.collectAsState()
    val searchResults by chatViewModel.searchResults.collectAsState()

    val profileImageUrl = userData?.profileImage ?: Constants.DEFAULT_PROFILE_IMAGE

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val premiumManager = remember { PremiumManager(context) }
    val isPremium = remember { mutableStateOf(premiumManager.isPremium()) }

    LaunchedEffect(Unit) {
        if (!isPremium.value) {
            InterstitialAdManager.loadAd(context)
            RewardedAdManager.loadAd(context)
        }
        chatViewModel.loadRecentQuestions()
    }

    // Search as user types
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            chatViewModel.searchHistory(searchQuery)
            isSearchActive = true
        } else {
            isSearchActive = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            if (!isPremium.value) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(BackgroundDark),
                    contentAlignment = Alignment.Center
                ) {
                    BannerAdView(adUnitId = Constants.BANNER_AD_ID)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Header Section (no welcome message)
            item {
                HomeHeaderSection(
                    profileImageUrl = profileImageUrl,
                    coins = coins,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    openDrawer = openDrawer,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onCoinClick = {
                        if (!isPremium.value) {
                            RewardedAdManager.showAd(context as Activity) {
                                authViewModel.addRewardCoin()
                            }
                        }
                    }
                )
            }

            // 2. Search Results (shown when searching)
            if (isSearchActive && searchResults.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text("SEARCH RESULTS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                items(searchResults.take(10)) { session ->
                    RecentChatItem(
                        session = session,
                        onClick = {
                            navController.navigate(Screen.Chat.route)
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            // 3. MAIN ACTIONS (hidden during search)
            if (!isSearchActive) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text("SOLVE A QUESTION", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SolveCard(
                                modifier = Modifier.weight(1f),
                                title = "Scan Question",
                                subtitle = "Point camera at problem",
                                cardIcon = Icons.Filled.CameraAlt,
                                onClick = { navController.navigate(Screen.Camera.route) },
                                iconTint = GradientStart
                            )
                            SolveCard(
                                modifier = Modifier.weight(1f),
                                title = "Type Question",
                                subtitle = "Type or paste question",
                                cardIcon = Icons.Filled.Keyboard,
                                onClick = { navController.navigate(Screen.Chat.route) },
                                iconTint = SuccessColor
                            )
                        }
                    }
                }

                // 4. QUICK ACTIONS
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text("QUICK ACTIONS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                title = "Upload PDF",
                                icon = Icons.Filled.PictureAsPdf,
                                onClick = { /* TODO */ }
                            )
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                title = "Generate Quiz",
                                icon = Icons.Filled.Quiz,
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // 5. RECENT QUESTIONS from real chat history
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RECENT QUESTIONS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            if (recentQuestions.isNotEmpty()) {
                                Text(
                                    "See All",
                                    color = GradientStart,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { navController.navigate(Screen.Chat.route) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (recentQuestions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Forum, null, tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No recent questions yet", color = TextSecondary, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Start a chat to see history here", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(recentQuestions.take(5)) { session ->
                        RecentChatItem(
                            session = session,
                            onClick = {
                                navController.navigate(Screen.Chat.route)
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }

                // 6. PRO BANNER
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        if (!isPremium.value) {
                            ProBanner(onClick = { navController.navigate(Screen.Premium.route) })
                        } else {
                            PremiumActiveBanner()
                        }
                    }
                }
            }
        }
    }
}

// ─── Header Section (no welcome message) ────────────────
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeHeaderSection(
    profileImageUrl: String,
    coins: Long,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    openDrawer: () -> Unit,
    onProfileClick: () -> Unit,
    onCoinClick: () -> Unit
) {
    val headerGradient = Brush.verticalGradient(colors = listOf(GradientStart, GradientEnd))
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = headerGradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(top = statusBarPadding + 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { openDrawer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Coin Display
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onCoinClick() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Stars, contentDescription = "Coins", tint = AccentGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    GlideImage(
                        model = profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App title instead of welcome message
            Text("AI Study Solver", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your smart study companion", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search past questions...", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, "Clear", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color.White.copy(alpha = 0.25f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
        }
    }
}

// ─── Recent Chat Item (real data) ────────────────────────
@Composable
fun RecentChatItem(session: ChatSession, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val timeAgo = remember(session.updatedAt) {
        DateUtils.getRelativeTimeSpanString(
            session.updatedAt.toDate().time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    Surface(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlinedDark)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(GradientStart.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChatBubbleOutline, null, tint = GradientStart, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    session.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(timeAgo, color = TextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── Existing components ─────────────────────────────────
@Composable
fun SolveCard(modifier: Modifier = Modifier, title: String, subtitle: String, cardIcon: ImageVector, onClick: () -> Unit, iconTint: Color) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = CardDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, OutlinedDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(cardIcon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier.size(28.dp).background(BackgroundDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = TextPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier = Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlinedDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = GradientEnd, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProBanner(onClick: () -> Unit) {
    val bannerGradient = Brush.horizontalGradient(colors = listOf(Color(0xFF7E57C2), Color(0xFFE91E63)))
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(bannerGradient)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Upgrade to PRO \uD83D\uDE80", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Unlimited AI answers • Faster results", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Upgrade", color = Color(0xFFE91E63), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PremiumActiveBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(AccentGold.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = "Premium", tint = AccentGold, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("AI Premium Active ✨", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("You have unlimited access", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
