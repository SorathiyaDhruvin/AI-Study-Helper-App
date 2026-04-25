package com.aistudy.solver

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aistudy.solver.navigation.NavGraph
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.*
import com.google.android.gms.ads.MobileAds
import com.aistudy.solver.utils.Constants
import com.aistudy.solver.utils.PremiumManager
import com.razorpay.Checkout
import com.razorpay.PaymentResultWithDataListener
import com.razorpay.PaymentData
import com.aistudy.solver.data.api.RetrofitClient
import com.aistudy.solver.data.api.OrderRequest
import com.aistudy.solver.data.api.VerificationRequest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    
    private var currentOrderId: String? = null
    private var currentAmountPaise: Int = 0
    private lateinit var premiumManager: PremiumManager
    private val isPremiumState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        premiumManager = PremiumManager(this)
        isPremiumState.value = premiumManager.isPremium()
        
        // Initialize AdMob
        MobileAds.initialize(this) { status ->
            Log.d("AdMob", "AdMob Initialized: $status")
        }

        Checkout.preload(applicationContext)

        setContent {
            val isDarkTheme = remember { mutableStateOf(false) } // Default Light Theme
            CompositionLocalProvider(LocalThemeMode provides isDarkTheme) {
                AIStudySolverTheme(darkTheme = isDarkTheme.value) {
                    MainScreen(isPremiumState.value)
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val orderId = paymentData?.orderId ?: ""
        Toast.makeText(this, "✅ Payment Successful!", Toast.LENGTH_SHORT).show()
        premiumManager.setPremium(true)
        isPremiumState.value = true

        lifecycleScope.launch {
            com.aistudy.solver.data.repository.UserRepository().savePaymentToFirestore(
                orderId, razorpayPaymentId ?: "", currentAmountPaise, "success"
            )
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        val msg = when (code) {
            0 -> "Payment cancelled"
            1 -> "Network error. Please try again."
            2 -> "Invalid options"
            else -> "Payment failed (Code $code)"
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        lifecycleScope.launch {
            com.aistudy.solver.data.repository.UserRepository().savePaymentToFirestore(
                currentOrderId ?: "", "N/A", currentAmountPaise, "failed"
            )
        }
    }

    fun startPayment(amountPaise: Int, planName: String) {
        currentAmountPaise = amountPaise
        val checkout = Checkout()
        checkout.setKeyID(Constants.RAZORPAY_KEY_ID)

        try {
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

            val options = JSONObject()
            options.put("name", "AI Study Solver")
            options.put("description", "$planName Subscription")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#3B82F6")
            options.put("currency", "INR")
            options.put("amount", amountPaise)

            val prefill = JSONObject()
            prefill.put("email", userEmail)
            options.put("prefill", prefill)

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            checkout.open(this, options)
        } catch (e: Exception) {
            Log.e("Payment", "Checkout Error", e)
            Toast.makeText(this, "Error starting payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainScreen(isPremium: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val authViewModel: com.aistudy.solver.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val isDataLoading by authViewModel.isDataLoading.collectAsState()
    
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed) // closed by default
    val scope = rememberCoroutineScope()

    val showBottomNavAndDrawer = currentRoute in listOf(
        Screen.Home.route,
        Screen.Profile.route,
        Screen.Settings.route,
        Screen.Premium.route
    )

    // Only show loading for screens that depend on user data
    val isAuthScreen = currentRoute == Screen.Login.route || 
                      currentRoute == Screen.Register.route || 
                      currentRoute == Screen.Splash.route ||
                      currentRoute == Screen.ForgotPassword.route ||
                      currentRoute == Screen.Onboarding.route
    
    // Use a single ModalNavigationDrawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomNavAndDrawer,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            if (showBottomNavAndDrawer) {
                val userData by authViewModel.userData.collectAsState()
                
                ModalDrawerSheet(
                    drawerContainerColor = BackgroundDark,
                    modifier = Modifier.width(300.dp)
                ) {
                    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val brush = if (isPremium) {
                        Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                    } else {
                        Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush)
                            .padding(top = statusBarPadding + 24.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("AI Study Solver", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            
                            if (isPremium) {
                                Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp).offset(y = 4.dp)) {
                                    Text("Premium Member ✨", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(modifier = Modifier.background(AccentGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp).offset(y = 4.dp)) {
                                    Text("Basic Plan", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlideImage(
                                        model = userData?.profileImage ?: Constants.DEFAULT_PROFILE_IMAGE,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(userData?.username ?: "Guest Student", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(userData?.email ?: "Join us to save progress", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerCustomItem(icon = Icons.Filled.Home, label = "Dashboard", isSelected = currentRoute == Screen.Home.route) {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Home.route) { popUpTo(navController.graph.findStartDestination().id); launchSingleTop = true }
                        }
                    }
                    DrawerCustomItem(icon = Icons.Filled.History, label = "My Solved Questions", isSelected = false) { scope.launch { drawerState.close() } }
                    
                    if (!isPremium) {
                        DrawerCustomItem(icon = Icons.Filled.WorkspacePremium, label = "Get Premium ✨", isSelected = currentRoute == Screen.Premium.route, tint = AccentGold) {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Premium.route) { popUpTo(navController.graph.findStartDestination().id); launchSingleTop = true }
                            }
                        }
                    }
                    
                    DrawerCustomItem(icon = Icons.Filled.Settings, label = "App Settings", isSelected = currentRoute == Screen.Settings.route) {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Settings.route) { popUpTo(navController.graph.findStartDestination().id); launchSingleTop = true }
                        }
                    }
                    DrawerCustomItem(icon = Icons.Filled.SupportAgent, label = "Help & Support", isSelected = false) { scope.launch { drawerState.close() } }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerCustomItem(icon = Icons.Filled.Logout, label = "Log Out", isSelected = false, tint = Color.Red.copy(alpha = 0.8f)) { 
                        scope.launch {
                            drawerState.close()
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (showBottomNavAndDrawer) {
                        NavigationBar(
                            containerColor = CardDark,
                            contentColor = TextSecondary,
                            modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        ) {
                            BottomNavItem(navController, currentRoute, "Home", Icons.Filled.Home, Screen.Home.route)
                            BottomNavItem(navController, currentRoute, "Scan", Icons.Filled.DocumentScanner, Screen.Camera.route)
                            BottomNavItem(navController, currentRoute, "Chat", Icons.Filled.ChatBubble, Screen.Chat.route)
                            BottomNavItem(navController, currentRoute, "Profile", Icons.Filled.Person, Screen.Profile.route)
                        }
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    NavGraph(
                        navController = navController, 
                        innerPadding = innerPadding, 
                        openDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }

            // Global Loading Overlay
            if (isDataLoading && !isAuthScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark.copy(alpha = 0.8f))
                        .clickable(enabled = false) {}, // Block interaction
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GradientStart)
                }
            }
        }
    }
}


@Composable
fun RowScope.BottomNavItem(navController: NavController, currentRoute: String?, label: String, icon: ImageVector, route: String) {
    val isSelected = currentRoute == route
    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
        selected = isSelected,
        onClick = {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = GradientStart,
            unselectedIconColor = TextSecondary,
            selectedTextColor = GradientStart,
            unselectedTextColor = TextSecondary,
            indicatorColor = GradientStart.copy(alpha = 0.1f)
        )
    )
}

@Composable
fun DrawerCustomItem(icon: ImageVector, label: String, isSelected: Boolean, tint: Color? = null, onClick: () -> Unit) {
    val iconColor = tint ?: (if (isSelected) GradientStart else TextSecondary)
    val bgColor = if (isSelected) GradientStart.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = if (isSelected) TextPrimary else TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

