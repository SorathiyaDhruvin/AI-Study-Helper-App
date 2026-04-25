package com.aistudy.solver.data.api

import com.aistudy.solver.data.model.GeminiRequest
import com.aistudy.solver.data.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}


