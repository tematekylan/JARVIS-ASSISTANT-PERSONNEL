package com.example.engine.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.entity.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MultiAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContentStream(
        settings: UserSettingsEntity,
        prompt: String,
        systemInstruction: String? = null,
        bitmap: Bitmap? = null,
        history: List<Pair<String, String>> = emptyList(),
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val provider = settings.activeAiProvider.lowercase()

        when (provider) {
            "openai" -> {
                val apiKey = settings.customOpenAiApiKey.trim()
                if (apiKey.isBlank()) {
                    throw IllegalStateException("Veuillez renseigner votre clé API OpenAI dans les Paramètres.")
                }
                streamOpenAiCompatible(
                    endpointUrl = "https://api.openai.com/v1/chat/completions",
                    apiKey = apiKey,
                    model = "gpt-4o",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    onChunkReceived = onChunkReceived
                )
            }
            "claude" -> {
                val apiKey = settings.customClaudeApiKey.trim()
                if (apiKey.isBlank()) {
                    throw IllegalStateException("Veuillez renseigner votre clé API Anthropic Claude dans les Paramètres.")
                }
                streamAnthropicClaude(
                    apiKey = apiKey,
                    model = "claude-3-5-sonnet-20241022",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    onChunkReceived = onChunkReceived
                )
            }
            "groq" -> {
                val apiKey = settings.customGroqApiKey.trim()
                if (apiKey.isBlank()) {
                    throw IllegalStateException("Veuillez renseigner votre clé API Groq dans les Paramètres.")
                }
                streamOpenAiCompatible(
                    endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    model = "llama-3.3-70b-versatile",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    onChunkReceived = onChunkReceived
                )
            }
            "deepseek" -> {
                val apiKey = settings.customDeepSeekApiKey.trim()
                if (apiKey.isBlank()) {
                    throw IllegalStateException("Veuillez renseigner votre clé API DeepSeek dans les Paramètres.")
                }
                streamOpenAiCompatible(
                    endpointUrl = "https://api.deepseek.com/v1/chat/completions",
                    apiKey = apiKey,
                    model = "deepseek-chat",
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    history = history,
                    onChunkReceived = onChunkReceived
                )
            }
            else -> {
                // Default: Google Gemini
                val customKey = settings.customGeminiApiKey.trim()
                val effectiveKey = if (customKey.isNotBlank()) {
                    customKey
                } else {
                    try {
                        BuildConfig.GEMINI_API_KEY
                    } catch (_: Exception) {
                        ""
                    }
                }

                if (effectiveKey.isBlank() || effectiveKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("API_KEY_NOT_CONFIGURED")
                }

                streamGoogleGemini(
                    apiKey = effectiveKey,
                    modelName = settings.aiModel,
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    bitmap = bitmap,
                    history = history,
                    onChunkReceived = onChunkReceived
                )
            }
        }
    }

    private fun normalizeGeminiModel(rawModel: String): String {
        val lower = rawModel.lowercase().trim()
        return when {
            lower.contains("3.5") -> "gemini-3.5-flash"
            lower.contains("3.1") && lower.contains("pro") -> "gemini-3.1-pro-preview"
            lower.contains("2.5") && lower.contains("pro") -> "gemini-2.5-pro"
            lower.contains("2.5") -> "gemini-2.5-flash"
            lower.contains("flash-latest") -> "gemini-flash-latest"
            else -> "gemini-2.5-flash"
        }
    }

    private suspend fun streamGoogleGemini(
        apiKey: String,
        modelName: String,
        prompt: String,
        systemInstruction: String?,
        bitmap: Bitmap?,
        history: List<Pair<String, String>>,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val requestJson = JSONObject()

        if (!systemInstruction.isNullOrBlank()) {
            val sysObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", systemInstruction))
            sysObj.put("parts", partsArray)
            requestJson.put("systemInstruction", sysObj)
        }

        val contentsArray = JSONArray()
        for ((role, text) in history) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "user") "user" else "model")
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", text))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val currentParts = JSONArray()

        if (bitmap != null) {
            val base64 = bitmap.toBase64()
            val inlineData = JSONObject()
            inlineData.put("mimeType", "image/jpeg")
            inlineData.put("data", base64)
            currentParts.put(JSONObject().put("inlineData", inlineData))
        }

        currentParts.put(JSONObject().put("text", prompt))
        currentContent.put("parts", currentParts)
        contentsArray.put(currentContent)

        requestJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        requestJson.put("generationConfig", genConfig)

        val primaryModel = normalizeGeminiModel(modelName)
        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)

        // Try primary model, then fallback sequentially across valid versions
        val modelCandidates = listOf(primaryModel, "gemini-2.5-flash", "gemini-3.5-flash", "gemini-flash-latest", "gemini-2.5-pro").distinct()

        var lastException: Exception? = null
        for (model in modelCandidates) {
            try {
                return@withContext executeGeminiStreamRequest(model, apiKey, requestBody, onChunkReceived)
            } catch (e: Exception) {
                lastException = e
            }
        }

        throw lastException ?: RuntimeException("Impossible de se connecter aux serveurs Gemini.")
    }

    private suspend fun executeGeminiStreamRequest(
        targetModel: String,
        apiKey: String,
        requestBody: okhttp3.RequestBody,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:streamGenerateContent?key=$apiKey&alt=sse"
        val request = Request.Builder().url(url).post(requestBody).build()
        val fullResponseBuilder = StringBuilder()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                throw RuntimeException("Erreur Gemini ($targetModel - ${response.code}): $errBody")
            }

            val source = response.body?.source() ?: return@withContext ""
            val reader = source.inputStream().bufferedReader()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data: ")) {
                    val jsonStr = currentLine.removePrefix("data: ").trim()
                    if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                        try {
                            val chunkObj = JSONObject(jsonStr)
                            val candidates = chunkObj.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val content = firstCandidate.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val textPart = parts.getJSONObject(0).optString("text", "")
                                    if (textPart.isNotEmpty()) {
                                        fullResponseBuilder.append(textPart)
                                        withContext(Dispatchers.Main) {
                                            onChunkReceived(textPart)
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        fullResponseBuilder.toString()
    }

    private suspend fun streamOpenAiCompatible(
        endpointUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String?,
        history: List<Pair<String, String>>,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val requestJson = JSONObject()
        requestJson.put("model", model)
        requestJson.put("stream", true)

        val messagesArr = JSONArray()

        if (!systemInstruction.isNullOrBlank()) {
            messagesArr.put(JSONObject().put("role", "system").put("content", systemInstruction))
        }

        for ((role, text) in history) {
            messagesArr.put(JSONObject().put("role", if (role == "user") "user" else "assistant").put("content", text))
        }

        messagesArr.put(JSONObject().put("role", "user").put("content", prompt))
        requestJson.put("messages", messagesArr)

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val fullResponseBuilder = StringBuilder()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                throw RuntimeException("Erreur API ($model - ${response.code}): $errBody")
            }

            val source = response.body?.source() ?: return@withContext ""
            val reader = source.inputStream().bufferedReader()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data: ")) {
                    val jsonStr = currentLine.removePrefix("data: ").trim()
                    if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                        try {
                            val chunkObj = JSONObject(jsonStr)
                            val choices = chunkObj.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val delta = choices.getJSONObject(0).optJSONObject("delta")
                                val content = delta?.optString("content", "") ?: ""
                                if (content.isNotEmpty()) {
                                    fullResponseBuilder.append(content)
                                    withContext(Dispatchers.Main) {
                                        onChunkReceived(content)
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        fullResponseBuilder.toString()
    }

    private suspend fun streamAnthropicClaude(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String?,
        history: List<Pair<String, String>>,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val requestJson = JSONObject()
        requestJson.put("model", model)
        requestJson.put("max_tokens", 4096)
        requestJson.put("stream", true)

        if (!systemInstruction.isNullOrBlank()) {
            requestJson.put("system", systemInstruction)
        }

        val messagesArr = JSONArray()
        for ((role, text) in history) {
            messagesArr.put(JSONObject().put("role", if (role == "user") "user" else "assistant").put("content", text))
        }
        messagesArr.put(JSONObject().put("role", "user").put("content", prompt))
        requestJson.put("messages", messagesArr)

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestBody)
            .build()

        val fullResponseBuilder = StringBuilder()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                throw RuntimeException("Erreur Claude (${response.code}): $errBody")
            }

            val source = response.body?.source() ?: return@withContext ""
            val reader = source.inputStream().bufferedReader()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.startsWith("data: ")) {
                    val jsonStr = currentLine.removePrefix("data: ").trim()
                    if (jsonStr.isNotBlank()) {
                        try {
                            val chunkObj = JSONObject(jsonStr)
                            val type = chunkObj.optString("type")
                            if (type == "content_block_delta") {
                                val delta = chunkObj.optJSONObject("delta")
                                val text = delta?.optString("text", "") ?: ""
                                if (text.isNotEmpty()) {
                                    fullResponseBuilder.append(text)
                                    withContext(Dispatchers.Main) {
                                        onChunkReceived(text)
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        fullResponseBuilder.toString()
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
