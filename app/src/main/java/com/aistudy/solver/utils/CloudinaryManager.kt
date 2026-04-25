package com.aistudy.solver.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryManager {
    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            try {
                val config = hashMapOf(
                    "cloud_name" to Constants.CLOUDINARY_CLOUD_NAME,
                    "api_key" to Constants.CLOUDINARY_API_KEY,
                    "api_secret" to Constants.CLOUDINARY_API_SECRET,
                    "secure" to true
                )
                MediaManager.init(context, config)
                isInitialized = true
            } catch (e: Exception) {
                // If already initialized by system or other means
                isInitialized = true
            }
        }
    }

    suspend fun uploadImage(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        try {
            MediaManager.get().upload(uri)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String
                        continuation.resume(url)
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("Cloudinary", error?.description ?: "Upload error")
                        continuation.resume(null)
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        continuation.resume(null)
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            Log.e("Cloudinary", "Must call init() before accessing Cloudinary", e)
            continuation.resume(null)
        }
    }
}
