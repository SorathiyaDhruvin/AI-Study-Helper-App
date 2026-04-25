package com.aistudy.solver.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class OcrHelper(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromUri(uri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.trim()
                    if (text.isNotEmpty() && continuation.isActive) {
                        continuation.resume(Result.success(text))
                    } else if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception("No readable text found in image")))
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    suspend fun extractTextFromBitmap(bitmap: android.graphics.Bitmap): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text.trim()
                    if (text.isNotEmpty() && continuation.isActive) {
                        continuation.resume(Result.success(text))
                    } else if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception("No readable text found in image")))
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }
}


