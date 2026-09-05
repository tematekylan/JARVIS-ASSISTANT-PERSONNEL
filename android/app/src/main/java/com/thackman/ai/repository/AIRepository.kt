package com.thackman.ai.repository

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(apiKey: String = "") {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        systemInstruction = com.google.ai.client.generativeai.type.content {
            text("""
                Tu es T-HACKMAN AI, un assistant personnel cybernétique d'élite.
                Tu réponds en français de manière claire, concise, technologique et bienveillante.
                Tu gères les tâches, les analyses et les automatisations.
            """.trimIndent())
        }
    )

    suspend fun queryAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Aucune information retournée par le noyau quantique."
        } catch (e: Exception) {
            "Erreur de communication : ${e.localizedMessage}"
        }
    }
}
