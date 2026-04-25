package com.aistudy.solver.data.model

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val role: String? = "user",
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)


