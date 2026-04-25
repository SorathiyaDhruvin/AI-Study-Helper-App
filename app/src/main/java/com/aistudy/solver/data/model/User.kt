package com.aistudy.solver.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "", // This is usually the doc ID, but keeping it in the model is fine
    
    @get:PropertyName("User Name")
    @set:PropertyName("User Name")
    var username: String = "",
    
    @get:PropertyName("Email")
    @set:PropertyName("Email")
    var email: String = "",
    
    @get:PropertyName("ProfileImage")
    @set:PropertyName("ProfileImage")
    var profileImage: String = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
    
    @get:PropertyName("Coins")
    @set:PropertyName("Coins")
    var coins: Long = 0,
    
    @get:PropertyName("Premium")
    @set:PropertyName("Premium")
    var premium: Boolean = false,
    
    @get:PropertyName("CreateAt")
    @set:PropertyName("CreateAt")
    var createAt: Timestamp? = null,
    
    @get:PropertyName("LastLogin")
    @set:PropertyName("LastLogin")
    var lastLogin: Timestamp? = null
)
