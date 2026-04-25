package com.aistudy.solver.data.model

import com.google.firebase.Timestamp

data class ChatSession(
    val id: String = "",
    val title: String = "New Chat",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val userId: String = "",
    val firstQuestion: String = ""
)

data class ChatMessage(
    val id: String = "",
    val sessionId: String = "",
    val text: String = "",
    val isAi: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "text", // "text", "image", "file"
    val mediaUrl: String = "",
    val fileName: String = ""
)
