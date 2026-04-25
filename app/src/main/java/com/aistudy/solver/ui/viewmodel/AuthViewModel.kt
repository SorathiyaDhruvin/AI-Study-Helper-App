package com.aistudy.solver.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudy.solver.data.model.User
import com.aistudy.solver.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import android.net.Uri

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object UserNotFound : AuthState() // Will be used for "Account not found. Please register first."
    object EmailAlreadyInUse : AuthState() // Will be used for "This email is already registered. Please do the login."
    object WrongPassword : AuthState()
    object UseGoogleLogin : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _coins = MutableStateFlow(0L)
    val coins: StateFlow<Long> = _coins

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _isDataLoading = MutableStateFlow(true)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth.currentUser != null) {
            viewModelScope.launch {
                loadUserData()
            }
        } else {
            _isDataLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
                userRepository.updateLastLogin()
                loadUserData()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.UserNotFound
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Login failed"
                when {
                    errorMsg.contains("wrong-password", ignoreCase = true) || 
                    errorMsg.contains("invalid-credential", ignoreCase = true) ||
                    errorMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> {
                        try {
                            val methods = auth.fetchSignInMethodsForEmail(email.trim()).await().signInMethods
                            if (methods?.contains(com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID) == true && 
                                !methods.contains(com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID)) {
                                _authState.value = AuthState.UseGoogleLogin
                            } else {
                                _authState.value = AuthState.WrongPassword
                            }
                        } catch (ex: Exception) {
                            _authState.value = AuthState.WrongPassword
                        }
                    }
                    errorMsg.contains("user-not-found", ignoreCase = true) ||
                    errorMsg.contains("no user", ignoreCase = true) -> {
                        _authState.value = AuthState.UserNotFound
                    }
                    else -> {
                        _authState.value = AuthState.Error(errorMsg)
                    }
                }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                
                // Assign a default profile image during registration
                // Image selection is now handled after reaching the dashboard via Profile screen
                val defaultImageUrl = com.aistudy.solver.utils.Constants.DEFAULT_PROFILE_IMAGE
                
                userRepository.createUserProfile(username, email, defaultImageUrl)
                loadUserData()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.EmailAlreadyInUse
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false
                
                if (isNewUser) {
                    val user = result.user
                    userRepository.createUserProfile(
                        user?.displayName ?: "Google User",
                        user?.email ?: "",
                        user?.photoUrl?.toString() ?: com.aistudy.solver.utils.Constants.DEFAULT_PROFILE_IMAGE
                    )
                } else {
                    // Even for existing users, ensure they have a profile in Firestore
                    // In case they signed up but profile creation failed or was manually deleted
                    val data = userRepository.getUserData()
                    if (data == null) {
                        val user = result.user
                        userRepository.createUserProfile(
                            user?.displayName ?: "Google User",
                            user?.email ?: "",
                            user?.photoUrl?.toString() ?: com.aistudy.solver.utils.Constants.DEFAULT_PROFILE_IMAGE
                        )
                    } else {
                        userRepository.updateLastLogin()
                    }
                }
                loadUserData()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Google login failed: ${e.message}")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Idle // Success but back to idle for message display
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }

    fun updateProfile(username: String, imageUri: Uri? = null, currentImageUrl: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val profileImageUrl = if (imageUri != null) {
                    userRepository.uploadProfileImage(imageUri) ?: currentImageUrl
                } else {
                    currentImageUrl
                }
                
                userRepository.updateUserProfile(username, profileImageUrl)
                loadUserData()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    private suspend fun loadUserData() {
        if (auth.currentUser == null) {
            _isDataLoading.value = false
            return
        }
        _isDataLoading.value = true
        try {
            val data = userRepository.getUserData()
            _userData.value = data
            _coins.value = data?.coins ?: 0
        } finally {
            _isDataLoading.value = false
        }
    }

    fun addRewardCoin() {
        viewModelScope.launch {
            userRepository.addCoin()
            loadUserData()
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _userData.value = null
        _coins.value = 0
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
