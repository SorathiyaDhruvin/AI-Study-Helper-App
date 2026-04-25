package com.aistudy.solver.data.repository

import com.aistudy.solver.data.api.RetrofitClient
import com.aistudy.solver.data.model.Content
import com.aistudy.solver.data.model.GeminiRequest
import com.aistudy.solver.data.model.Part
import com.aistudy.solver.utils.Constants
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SolveRepository {

    suspend fun getSolution(question: String): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        val maxRetries = 2
        
        Log.d("SolveRepository", "Requesting solution from Gemini API...")
        
        for (attempt in 0..maxRetries) {
            try {
                val prompt = """
                    You are an AI assistant.
                    The input text is extracted using OCR and may contain errors, special characters, or distorted words.
                    
                    Your task:
                    1. First clean and correct the OCR text
                    2. Convert it into proper readable English
                    3. Then understand the question
                    4. Finally generate a clear and correct solution
                    
                    Do not include garbage characters or symbols.
                    If text is unclear, intelligently guess the correct meaning.
                    
                    Output format:
                    - Cleaned Question: [Write corrected question here]
                    - Final Solution: [Write step-by-step solution here]
                    
                    Input OCR Text:
                    $question
                """.trimIndent()
                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    )
                )

                val response = RetrofitClient.api.generateContent(Constants.GEMINI_API_KEY, request)
                if (response.isSuccessful) {
                    val answerText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (answerText != null) {
                        Log.d("SolveRepository", "Solution received successfully.")
                        return@withContext Result.success(answerText)
                    } else {
                        Log.e("SolveRepository", "Empty response body from API")
                        return@withContext Result.failure(Exception("AI returned empty response. Try again."))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() 
                    Log.e("SolveRepository", "API Error Code: ${response.code()}, Body: $errorBody")
                    val errorMsg = when (response.code()) {
                        401 -> "Invalid API key"
                        403 -> "API not enabled"
                        404 -> "Model not found"
                        429 -> "Too many requests. Please wait a moment."
                        500, 502, 503 -> "Server issue, please try again later"
                        else -> "Something went wrong (Code: ${response.code()})"
                    }
                    return@withContext Result.failure(Exception(errorMsg))
                }
            } catch (e: java.net.SocketTimeoutException) {
                lastException = Exception("Slow network, please wait (Timeout)")
                Log.w("SolveRepository", "Timeout on attempt $attempt: ${e.message}")
                if (attempt < maxRetries) {
                    continue
                }
            } catch (e: java.io.IOException) {
                lastException = Exception("Check your connection")
                Log.e("SolveRepository", "IO Error: ${e.message}")
                break 
            } catch (e: Exception) {
                lastException = Exception("Server issue, try again")
                Log.e("SolveRepository", "General Error: ${e.message}")
                break
            }
        }
        return@withContext Result.failure(lastException ?: Exception("Connection error"))
    }
}
