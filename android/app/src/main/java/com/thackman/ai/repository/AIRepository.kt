package com.thackman.ai.repository

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(context: Context? = null, apiKey: String? = null) {
    
    // Try to get API key from BuildConfig, environment, or parameter
    private val actualApiKey: String = apiKey 
        ?: System.getenv("GEMINI_API_KEY") 
        ?: getApiKeyFromPreferences(context)
        ?: "AIzaSyDummyKey_PleaseSetYourActualKey" // Fallback (will fail gracefully)
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = actualApiKey,
            systemInstruction = content {
                text("""
                    Tu es T-HACKMAN AI, un assistant personnel cybernétique d'élite.
                    Tu réponds en français de manière claire, concise, technologique et bienveillante.
                    Tu gères les tâches, les analyses et les automatisations.
                    Utilise des références cyberpunk et futuristes dans tes réponses.
                """.trimIndent())
            }
        )
    }

    suspend fun queryAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            if (actualApiKey.contains("DummyKey") || actualApiKey.isEmpty()) {
                return@withContext "⚠️ ERREUR SYSTÈME : Clé API Gemini non configurée. " +
                    "Veuillez définir GEMINI_API_KEY dans les variables d'environnement ou SharedPreferences."
            }
            
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Aucune information retournée par le noyau quantique."
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Erreur inconnue"
            if (errorMsg.contains("API key")) {
                "❌ DÉFAILLANCE D'AUTHENTIFICATION : ${e.localizedMessage}"
            } else {
                "⚠️ Erreur de communication IA : $errorMsg"
            }
        }
    }
    
    private fun getApiKeyFromPreferences(context: Context?): String? {
        return try {
            context?.let {
                val prefs = it.getSharedPreferences("thackman_config", Context.MODE_PRIVATE)
                prefs.getString("gemini_api_key", null)
            }
        } catch (e: Exception) {
            null
        }
    }
}
