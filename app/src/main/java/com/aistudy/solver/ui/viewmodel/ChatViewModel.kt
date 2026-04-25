package com.aistudy.solver.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudy.solver.data.model.ChatMessage
import com.aistudy.solver.data.model.ChatSession
import com.aistudy.solver.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()

    // Current active session
    private val _currentSession = MutableStateFlow<ChatSession?>(null)
    val currentSession: StateFlow<ChatSession?> = _currentSession

    // Messages in current session
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // All chat sessions for sidebar
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions

    // Search results
    private val _searchResults = MutableStateFlow<List<ChatSession>>(emptyList())
    val searchResults: StateFlow<List<ChatSession>> = _searchResults

    // Recent questions for home screen
    private val _recentQuestions = MutableStateFlow<List<ChatSession>>(emptyList())
    val recentQuestions: StateFlow<List<ChatSession>> = _recentQuestions

    // Loading / typing states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    init {
        loadChatSessions()
        loadRecentQuestions()
    }

    // ─── Session Management ─────────────────────────────

    fun loadChatSessions() {
        viewModelScope.launch {
            _chatSessions.value = chatRepository.getChatSessions()
        }
    }

    fun loadRecentQuestions() {
        viewModelScope.launch {
            _recentQuestions.value = chatRepository.getRecentQuestions(10)
        }
    }

    fun startNewChat() {
        _currentSession.value = null
        _messages.value = emptyList()
    }

    fun openChat(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val sessions = _chatSessions.value
            val session = sessions.find { it.id == chatId }
            _currentSession.value = session
            _messages.value = chatRepository.getMessages(chatId)
            _isLoading.value = false
        }
    }

    fun renameChat(chatId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateChatTitle(chatId, newTitle)
            loadChatSessions()
            // Also update current session if it's the one being renamed
            if (_currentSession.value?.id == chatId) {
                _currentSession.value = _currentSession.value?.copy(title = newTitle)
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChatSession(chatId)
            if (_currentSession.value?.id == chatId) {
                startNewChat()
            }
            loadChatSessions()
            loadRecentQuestions()
        }
    }

    fun clearConversation() {
        val sessionId = _currentSession.value?.id ?: return
        viewModelScope.launch {
            chatRepository.clearMessages(sessionId)
            _messages.value = emptyList()
        }
    }

    // ─── Search ─────────────────────────────────────────

    fun searchHistory(query: String) {
        viewModelScope.launch {
            _searchResults.value = chatRepository.searchChatHistory(query)
        }
    }

    // ─── Messaging ──────────────────────────────────────

    fun sendMessage(text: String, type: String = "text", mediaUrl: String = "", fileName: String = "") {
        if (text.isBlank() && mediaUrl.isBlank()) return

        viewModelScope.launch {
            try {
                // If no active session, create one
                var session = _currentSession.value
                if (session == null) {
                    session = chatRepository.createChatSession(text)
                    _currentSession.value = session
                }

                val chatId = session.id

                // Add user message
                val userMessage = chatRepository.addMessage(
                    chatId = chatId,
                    text = text,
                    isAi = false,
                    type = type,
                    mediaUrl = mediaUrl,
                    fileName = fileName
                )
                _messages.value = _messages.value + userMessage

                // Show typing indicator
                _isTyping.value = true

                // Fetch AI response
                val allMessages = _messages.value
                val response = fetchGeminiResponse(allMessages)

                // Add AI message
                val aiMessage = chatRepository.addMessage(
                    chatId = chatId,
                    text = response,
                    isAi = true
                )
                _messages.value = _messages.value + aiMessage
                _isTyping.value = false

                // Refresh sidebar
                loadChatSessions()
                loadRecentQuestions()

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
                _isTyping.value = false

                // Add error message locally (not to Firestore)
                val errorMsg = ChatMessage(
                    text = "Something went wrong. Please try again.",
                    isAi = true,
                    type = "text"
                )
                _messages.value = _messages.value + errorMsg
            }
        }
    }

    fun regenerateLastResponse() {
        viewModelScope.launch {
            val msgs = _messages.value.toMutableList()
            // Remove last AI message
            if (msgs.isNotEmpty() && msgs.last().isAi) {
                msgs.removeAt(msgs.size - 1)
                _messages.value = msgs

                _isTyping.value = true
                val response = fetchGeminiResponse(msgs)

                val chatId = _currentSession.value?.id ?: return@launch
                val aiMessage = chatRepository.addMessage(chatId, response, true)
                _messages.value = _messages.value + aiMessage
                _isTyping.value = false
            }
        }
    }

    // ─── Gemini API ─────────────────────────────────────

    private suspend fun fetchGeminiResponse(conversation: List<ChatMessage>): String {
        return withContext(Dispatchers.IO) {
            var lastError = "Something went wrong"
            val maxRetries = 2

            for (attempt in 0..maxRetries) {
                try {
                    val promptText = buildString {
                        append("You are an AI Study Helper. Provide concise, helpful answers formatting them nicely for mobile viewing. Use markdown-like formatting for clarity.\n\n")
                        for (msg in conversation) {
                            append(if (msg.isAi) "AI: " else "User: ")
                            append(msg.text)
                            append("\n\n")
                        }
                        append("AI: ")
                    }

                    val request = com.aistudy.solver.data.model.GeminiRequest(
                        contents = listOf(
                            com.aistudy.solver.data.model.Content(
                                role = "user",
                                parts = listOf(com.aistudy.solver.data.model.Part(text = promptText))
                            )
                        )
                    )

                    val apiResponse = com.aistudy.solver.data.api.RetrofitClient.api.generateContent(
                        com.aistudy.solver.utils.Constants.GEMINI_API_KEY,
                        request
                    )

                    if (apiResponse.isSuccessful) {
                        return@withContext apiResponse.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "AI returned empty response"
                    } else {
                        val errorBody = apiResponse.errorBody()?.string() ?: ""
                        Log.e("ChatViewModel", "API Error: ${apiResponse.code()} - $errorBody")
                        lastError = when (apiResponse.code()) {
                            401 -> "Invalid API key"
                            403 -> "API not enabled"
                            429 -> "Too many requests. Please wait a moment."
                            500, 502, 503 -> "Server issue, please try again later"
                            else -> "Server issue, try again"
                        }
                        break
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    lastError = "Slow network, please wait"
                    if (attempt < maxRetries) continue
                } catch (e: java.io.IOException) {
                    lastError = "Check your connection"
                    break
                } catch (e: Exception) {
                    lastError = "Server issue, try again"
                    break
                }
            }
            lastError
        }
    }
}
