package com.aistudy.solver.data.repository

import android.util.Log
import com.aistudy.solver.data.model.ChatMessage
import com.aistudy.solver.data.model.ChatSession
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String = auth.currentUser?.uid ?: ""

    private fun chatsCollection() = db.collection("users").document(getUserId()).collection("chats")

    private fun messagesCollection(chatId: String) =
        chatsCollection().document(chatId).collection("messages")

    // ─── Chat Sessions ──────────────────────────────────────

    suspend fun createChatSession(firstQuestion: String = ""): ChatSession {
        val docRef = chatsCollection().document()
        val title = if (firstQuestion.length > 50) firstQuestion.take(50) + "..." else firstQuestion.ifBlank { "New Chat" }
        val session = ChatSession(
            id = docRef.id,
            title = title,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            userId = getUserId(),
            firstQuestion = firstQuestion
        )
        docRef.set(session).await()
        return session
    }

    suspend fun getChatSessions(): List<ChatSession> {
        return try {
            chatsCollection()
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(ChatSession::class.java)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting sessions: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateChatTitle(chatId: String, newTitle: String) {
        try {
            chatsCollection().document(chatId).update("title", newTitle).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error updating title: ${e.message}")
        }
    }

    suspend fun deleteChatSession(chatId: String) {
        try {
            // Delete all messages first
            val messages = messagesCollection(chatId).get().await()
            for (doc in messages.documents) {
                doc.reference.delete().await()
            }
            // Then delete the session
            chatsCollection().document(chatId).delete().await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting session: ${e.message}")
        }
    }

    private suspend fun touchSession(chatId: String) {
        try {
            chatsCollection().document(chatId).update("updatedAt", Timestamp.now()).await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error touching session: ${e.message}")
        }
    }

    // ─── Messages ──────────────────────────────────────────

    suspend fun addMessage(chatId: String, text: String, isAi: Boolean, type: String = "text", mediaUrl: String = "", fileName: String = ""): ChatMessage {
        val docRef = messagesCollection(chatId).document()
        val message = ChatMessage(
            id = docRef.id,
            sessionId = chatId,
            text = text,
            isAi = isAi,
            timestamp = Timestamp.now(),
            type = type,
            mediaUrl = mediaUrl,
            fileName = fileName
        )
        docRef.set(message).await()
        touchSession(chatId)
        return message
    }

    suspend fun getMessages(chatId: String): List<ChatMessage> {
        return try {
            messagesCollection(chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(ChatMessage::class.java)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting messages: ${e.message}")
            emptyList()
        }
    }

    suspend fun clearMessages(chatId: String) {
        try {
            val messages = messagesCollection(chatId).get().await()
            for (doc in messages.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error clearing messages: ${e.message}")
        }
    }

    // ─── Search ─────────────────────────────────────────────

    suspend fun searchChatHistory(query: String): List<ChatSession> {
        if (query.isBlank()) return getChatSessions().take(10)
        return try {
            val allSessions = getChatSessions()
            allSessions.filter { session ->
                session.title.contains(query, ignoreCase = true) ||
                session.firstQuestion.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error searching: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecentQuestions(limit: Int = 5): List<ChatSession> {
        return try {
            chatsCollection()
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .toObjects(ChatSession::class.java)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting recent: ${e.message}")
            emptyList()
        }
    }
}
