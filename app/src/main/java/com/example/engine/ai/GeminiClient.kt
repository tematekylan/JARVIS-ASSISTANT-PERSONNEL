package com.example.engine.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
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

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun normalizeModelName(rawModel: String): String {
        val lower = rawModel.lowercase().trim()
        return when {
            lower.contains("pro") -> "gemini-2.5-pro"
            lower.contains("2.0") -> "gemini-2.0-flash"
            lower.contains("1.5") && lower.contains("flash") -> "gemini-1.5-flash"
            lower.contains("1.5") && lower.contains("pro") -> "gemini-1.5-pro"
            // For flash, images, 2.1, 2.5, 3.5, 3.7 or default -> gemini-2.5-flash
            else -> "gemini-2.5-flash"
        }
    }

    suspend fun generateContentStream(
        modelName: String = "gemini-2.5-flash",
        prompt: String,
        systemInstruction: String? = null,
        bitmap: Bitmap? = null,
        history: List<Pair<String, String>> = emptyList(),
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API_KEY_NOT_CONFIGURED")
        }

        val requestJson = JSONObject()

        // System instruction
        if (!systemInstruction.isNullOrBlank()) {
            val sysObj = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", systemInstruction))
            sysObj.put("parts", partsArray)
            requestJson.put("systemInstruction", sysObj)
        }

        // Contents
        val contentsArray = JSONArray()

        // Conversation history
        for ((role, text) in history) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "user") "user" else "model")
            val partsArr = JSONArray()
            partsArr.put(JSONObject().put("text", text))
            contentObj.put("parts", partsArr)
            contentsArray.put(contentObj)
        }

        // Current message
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

        // Generation config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        requestJson.put("generationConfig", genConfig)

        val normalizedModel = normalizeModelName(modelName)
        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)

        try {
            executeStreamRequest(normalizedModel, apiKey, requestBody, onChunkReceived)
        } catch (e: Exception) {
            // If primary model failed (e.g. 404 on preview endpoint), fallback to standard gemini-2.5-flash or gemini-1.5-flash
            if (normalizedModel != "gemini-2.5-flash") {
                try {
                    executeStreamRequest("gemini-2.5-flash", apiKey, requestBody, onChunkReceived)
                } catch (_: Exception) {
                    executeStreamRequest("gemini-1.5-flash", apiKey, requestBody, onChunkReceived)
                }
            } else {
                try {
                    executeStreamRequest("gemini-1.5-flash", apiKey, requestBody, onChunkReceived)
                } catch (_: Exception) {
                    throw e
                }
            }
        }
    }

    private suspend fun executeStreamRequest(
        targetModel: String,
        apiKey: String,
        requestBody: okhttp3.RequestBody,
        onChunkReceived: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:streamGenerateContent?key=$apiKey&alt=sse"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val fullResponseBuilder = StringBuilder()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                throw RuntimeException("Gemini API error (${response.code}): $errBody")
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
                        } catch (_: Exception) {
                            // Ignored formatting chunk
                        }
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
