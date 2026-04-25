package com.aistudy.solver.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aistudy.solver.ui.screens.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.aistudy.solver.ui.viewmodel.SolveViewModel
import com.aistudy.solver.ui.viewmodel.AuthViewModel
import com.aistudy.solver.ui.viewmodel.ChatViewModel

@Composable
fun NavGraph(navController: NavHostController, innerPadding: PaddingValues, openDrawer: () -> Unit) {
    val solveViewModel: SolveViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(innerPadding),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, openDrawer)
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController, solveViewModel)
        }
        composable(Screen.Result.route) {
            ResultScreen(navController, solveViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController, authViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController, authViewModel)
        }
        composable(Screen.Premium.route) {
            PremiumScreen(navController)
        }
        
        // Auth & Onboarding Flow
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController, authViewModel)
        }
        
        // AI Tutor / Chat Flow — pass shared ChatViewModel
        composable(Screen.Chat.route) {
            ChatScreen(navController, chatViewModel)
        }
        
        // Status & Ads Flow
        composable(Screen.Error.route) {
            ErrorScreen(navController)
        }
        composable(Screen.AdDisplay.route) {
            AdDisplayScreen(navController)
        }
    }
}
