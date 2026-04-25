package com.aistudy.solver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.ui.theme.*

@Composable
fun ErrorScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("No Internet") }
    val tabs = listOf("No Internet", "API Error", "Camera")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CardDark, RoundedCornerShape(12.dp))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Status", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 48.dp))
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .border(1.dp, if (isSelected) PrimaryColor else OutlinedDark, RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tab, color = if (isSelected) PrimaryColor else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        val icon = when (selectedTab) {
            "API Error" -> Icons.Filled.Warning
            "Camera" -> Icons.Filled.SettingsCell
            else -> Icons.Filled.WifiOff
        }
        
        val title = when (selectedTab) {
            "API Error" -> "Server Unreachable"
            "Camera" -> "Camera Permission Denied"
            else -> "No Internet Connection"
        }
        
        val message = when (selectedTab) {
            "API Error" -> "The AI server is currently busy or down. Please try your request again shortly."
            "Camera" -> "AI Study Solver needs camera access to scan your questions. Please enable it in settings."
            else -> "It seems you're offline. Please check your Wi-Fi or mobile data and try again."
        }

        Icon(icon, contentDescription = "Error Icon", tint = TextSecondary, modifier = Modifier.size(80.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Retry Action */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Retry Connection", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AdDisplayScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text("REWARD AD", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(AccentGold.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = "Gift", tint = AccentGold, modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("4", color = GradientStart, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Watch to Unlock Full Answer", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Watch a short ad to get the complete step-by-step solution for free",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(CardDark, RoundedCornerShape(16.dp))
                .border(1.dp, OutlinedDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Tv, contentDescription = "Ad", tint = PrimaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ad content appears here", color = PrimaryColor, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Test Ad · Google AdMob", color = TextSecondary, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(CardDark, RoundedCornerShape(2.dp))) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(4.dp).background(GradientStart, RoundedCornerShape(2.dp)))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Skip after 5s \u203A", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.clickable { navController.popBackStack() })
    }
}


