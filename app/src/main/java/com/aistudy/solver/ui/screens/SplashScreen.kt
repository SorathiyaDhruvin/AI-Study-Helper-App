package com.aistudy.solver.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.AccentColor
import com.aistudy.solver.ui.theme.PrimaryColor
import com.aistudy.solver.ui.theme.SecondaryColor
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        if (auth.currentUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(PrimaryColor, SecondaryColor),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo Container with Glow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
                    .shadow(32.dp, MaterialTheme.shapes.extraLarge, ambientColor = PrimaryColor, spotColor = SecondaryColor)
                    .background(gradientBrush, shape = MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusStrong,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(100.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-20).dp, y = 20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            val appTitle = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("AI Study ")
                }
                withStyle(style = SpanStyle(color = PrimaryColor)) {
                    append("Solver")
                }
            }
            
            Text(
                text = appTitle,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(alphaAnim),
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Scan • Solve • Learn",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alpha(alphaAnim),
                letterSpacing = 1.sp
            )
        }
    }
}


