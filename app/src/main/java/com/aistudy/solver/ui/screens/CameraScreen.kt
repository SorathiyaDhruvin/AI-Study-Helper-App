package com.aistudy.solver.ui.screens

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.aistudy.solver.navigation.Screen
import com.aistudy.solver.ui.theme.AccentColor
import com.aistudy.solver.ui.theme.PrimaryColor
import com.aistudy.solver.ui.theme.SecondaryColor
import com.aistudy.solver.ui.viewmodel.SolveViewModel
import java.io.File
import androidx.camera.core.ImageCaptureException

@Composable
fun CameraScreen(navController: NavController, viewModel: SolveViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(Runnable {
                cameraProvider = cameraProviderFuture.get()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.processImage(context, uri)
            navController.navigate(Screen.Result.route)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B19))
            .systemBarsPadding()
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
            }
        }

        // Scanning Frame Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // CameraX Preview
            if (hasCameraPermission && cameraProvider != null) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            this.scaleType = PreviewView.ScaleType.FILL_CENTER
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider?.unbindAll()
                            cameraProvider?.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }
                )
            } else {
                CircularProgressIndicator(color = PrimaryColor)
            }

            // Animated Glowing Scanning Frame
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .aspectRatio(0.8f, matchHeightConstraintsFirst = false)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = PrimaryColor,
                        size = size,
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx()),
                        alpha = 0.8f
                    )

                    val yPos = size.height * offsetY
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, AccentColor, Color.Transparent)
                        ),
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 6.dp.toPx()
                    )
                    
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = yPos,
                            endY = yPos + 100f
                        ),
                        topLeft = Offset(0f, yPos),
                        size = Size(size.width, 100f)
                    )
                }
            }
        }

        // Bottom Controls Area
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Position question in frame",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Capture Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Brush.radialGradient(listOf(PrimaryColor, SecondaryColor)), CircleShape)
                    .shadow(16.dp, CircleShape, ambientColor = PrimaryColor, spotColor = PrimaryColor)
                    .clickable {
                        if (hasCameraPermission) {
                            val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                        viewModel.processImage(context, savedUri)
                                        navController.navigate(Screen.Result.route)
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        Toast.makeText(context, "Image capture failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(64.dp).background(Color.White, CircleShape)) {
                    Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape).border(2.dp, Color(0xFFE2E8F0), CircleShape).align(Alignment.Center))
                }
            }
        }
    }
}


