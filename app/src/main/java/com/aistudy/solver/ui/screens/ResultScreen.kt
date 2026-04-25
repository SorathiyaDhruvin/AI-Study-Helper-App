package com.aistudy.solver.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.ui.theme.PrimaryColor
import com.aistudy.solver.ui.viewmodel.SolveState
import com.aistudy.solver.ui.viewmodel.SolveViewModel
import com.aistudy.solver.ui.viewmodel.AuthViewModel
import com.aistudy.solver.utils.InterstitialAdManager
import com.aistudy.solver.utils.RewardedAdManager
import com.aistudy.solver.utils.PremiumManager
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, viewModel: SolveViewModel) {
    val authViewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val premiumManager = remember { PremiumManager(context) }
    val isPremium = remember { premiumManager.isPremium() }
    var hasShownInterstitial by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isPremium) {
            InterstitialAdManager.loadAd(context)
            RewardedAdManager.loadAd(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solution", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            when (val state = uiState) {
                is SolveState.Idle -> {}
                is SolveState.ExtractingText -> LoadingView("Extracting text from image...")
                is SolveState.Solving -> LoadingView("AI is solving the question...")
                is SolveState.Success -> {
                    if (!isPremium && !hasShownInterstitial) {
                        SideEffect {
                            InterstitialAdManager.showAd(context as Activity) {
                                hasShownInterstitial = true
                            }
                        }
                    }
                    SuccessView(question = state.question, answer = state.answer, isPremium = isPremium, onAdReward = { authViewModel.addRewardCoin() })
                }
                is SolveState.Error -> {
                    ErrorView(message = state.message) { navController.popBackStack() }
                }
            }
        }
    }
}

@Composable
fun SuccessView(question: String, answer: String, isPremium: Boolean, onAdReward: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    var isFullSolutionVisible by remember { mutableStateOf(isPremium) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Question", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(question, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("AI Solution", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        if (isPremium) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = "Premium", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isFullSolutionVisible) {
                        Text(answer, fontSize = 16.sp, lineHeight = 24.sp)
                    } else {
                        Text(
                            text = if (answer.length > 200) answer.take(200) + "..." else answer,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.graphicsLayer(alpha = 0.5f)
                        )
                        
                        if (answer.length > 200) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    val activity = context as? Activity
                                    RewardedAdManager.showAd(activity!!) {
                                        isFullSolutionVisible = true
                                        onAdReward()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Watch Ad to Unlock Full Solution")
                                }
                            }
                        } else {
                            isFullSolutionVisible = true 
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LoadingView(message: String) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = PrimaryColor, strokeWidth = 4.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = message, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Warning, contentDescription = "Error", tint = Color(0xFFE53935), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Oops! Something went wrong.", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
            Text("Try Again", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        }
    }
}
