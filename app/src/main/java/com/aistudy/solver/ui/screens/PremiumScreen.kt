package com.aistudy.solver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudy.solver.MainActivity
import com.aistudy.solver.ui.theme.*

@Composable
fun PremiumScreen(navController: NavController) {
    var selectedPlan by remember { mutableStateOf("yearly") } // "monthly" or "yearly"
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 48.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CardDark, RoundedCornerShape(12.dp))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Text("AI Study Solver", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Titles
        Text("Learn Smarter with", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("AI Premium", color = GradientStart, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(16.dp))

        Text("✨", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text("WHAT'S INCLUDED", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Features list
        Column(modifier = Modifier.padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PremiumFeatureBox("Unlimited AI Solutions")
            PremiumFeatureBox("No More Ads")
            PremiumFeatureBox("Faster Processing")
            PremiumFeatureBox("Step-by-step Math Explanations")
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Plans
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PlanBox(
                modifier = Modifier.weight(1f),
                title = "Monthly Plan",
                price = "₹5/mo",
                subtext = "Billed monthly",
                isSelected = selectedPlan == "monthly",
                onClick = { selectedPlan = "monthly" }
            )
            
            PlanBox(
                modifier = Modifier.weight(1f),
                title = "Yearly Plan",
                price = "₹15/yr",
                subtext = "One-time payment",
                isSelected = selectedPlan == "yearly",
                badge = "BEST VALUE",
                onClick = { selectedPlan = "yearly" }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Subscribe Button
        Button(
            onClick = {
                val activity = context as? MainActivity
                if (selectedPlan == "monthly") {
                    activity?.startPayment(50, "Monthly") // ₹5 in paise
                } else {
                    activity?.startPayment(150, "Yearly") // ₹15 in paise
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Upgrade to Premium", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cancel subscription anytime. No hidden charges.", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PremiumFeatureBox(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).background(SuccessColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Check", tint = SuccessColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun PlanBox(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    subtext: String,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) PrimaryColor.copy(alpha = 0.1f) else CardDark, RoundedCornerShape(20.dp))
                .border(2.dp, if (isSelected) PrimaryColor else OutlinedDark, RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = price, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtext, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
        }
        
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .background(AccentGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BackgroundDark)
            }
        }
    }
}


