package com.aistudy.solver.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash_screen", "Splash")
    object Onboarding : Screen("onboarding_screen", "Onboarding")
    object Login : Screen("login_screen", "Login")
    object Register : Screen("register_screen", "Register")
    
    // Core App
    object Home : Screen("home_screen", "Home", Icons.Filled.Home)
    object Camera : Screen("camera_screen", "Scan Photo")
    object Chat : Screen("chat_screen", "AI Tutor", Icons.Filled.Chat) // Type Question
    object Result : Screen("result_screen", "Solution")
    
    // Auxiliary App
    object Profile : Screen("profile_screen", "Profile", Icons.Filled.Person)
    object Premium : Screen("premium_screen", "Premium", Icons.Filled.Star)
    object Settings : Screen("settings_screen", "Settings", Icons.Filled.Settings)
    object Error : Screen("error_screen", "Status") // Includes Network, API, Camera errors
    object AdDisplay : Screen("ad_display_screen", "Ad View")
    object ForgotPassword : Screen("forgot_password", "Forgot Password")
}


