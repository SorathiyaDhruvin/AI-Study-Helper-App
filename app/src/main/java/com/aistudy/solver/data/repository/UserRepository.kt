package com.aistudy.solver.data.repository

import com.aistudy.solver.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import android.util.Log
import android.net.Uri
import com.aistudy.solver.utils.CloudinaryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun createUserProfile(username: String, email: String, profileImage: String? = null) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        val now = Timestamp.now()
        val user = User(
            uid = userId,
            username = username,
            email = email,
            profileImage = profileImage ?: com.aistudy.solver.utils.Constants.DEFAULT_PROFILE_IMAGE,
            coins = 0,
            premium = false,
            createAt = now,
            lastLogin = now
        )
        try {
            Log.d("UserRepository", "Creating user profile for: $email")
            db.collection("Users").document(userId).set(user).await()
            Log.d("UserRepository", "User profile created successfully.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating user profile", e)
        }
    }

    suspend fun getUserData(): User? = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext null
        return@withContext try {
            Log.d("UserRepository", "Fetching user data for: $userId")
            val document = db.collection("Users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            Log.d("UserRepository", "User data fetched: ${user?.username}")
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user data", e)
            null
        }
    }

    suspend fun uploadProfileImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "Uploading profile image...")
        CloudinaryManager.uploadImage(uri)
    }

    suspend fun updateUserProfile(username: String, profileImage: String) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            Log.d("UserRepository", "Updating profile for: $userId")
            db.collection("Users").document(userId)
                .update(
                    "User Name", username,
                    "ProfileImage", profileImage
                ).await()
            Log.d("UserRepository", "User profile updated successfully.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile", e)
        }
    }

    suspend fun updateLastLogin() = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            db.collection("Users").document(userId)
                .update("LastLogin", Timestamp.now()).await()
            Log.d("UserRepository", "Last login updated.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating last login", e)
        }
    }

    suspend fun getUserCoins(): Long = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext 0
        return@withContext try {
            val document = db.collection("Users").document(userId).get().await()
            document.getLong("Coins") ?: 0
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user coins", e)
            0
        }
    }

    suspend fun addCoin() = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            Log.d("UserRepository", "Adding coin to: $userId")
            db.collection("Users").document(userId)
                .update("Coins", FieldValue.increment(1)).await()
            Log.d("UserRepository", "Coin added successfully.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding coin", e)
        }
    }

    suspend fun updateProfileImage(imageUrl: String) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            db.collection("Users").document(userId)
                .update("ProfileImage", imageUrl).await()
            Log.d("UserRepository", "Profile image updated successfully.")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile image", e)
        }
    }
    suspend fun savePaymentToFirestore(
        orderId: String,
        paymentId: String,
        amount: Int,
        status: String
    ) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        val paymentData = hashMapOf(
            "userId" to userId,
            "orderId" to orderId,
            "paymentId" to paymentId,
            "amount" to amount,
            "status" to status,
            "timestamp" to Timestamp.now()
        )
        try {
            db.collection("Payments").document(paymentId).set(paymentData).await()
            if (status == "success") {
                db.collection("Users").document(userId).update("premium", true).await()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving payment", e)
        }
    }
}
