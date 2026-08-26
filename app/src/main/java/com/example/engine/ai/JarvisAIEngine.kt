package com.example.engine.ai

import android.graphics.Bitmap
import com.example.data.JarvisRepository
import com.example.data.entity.UserSettingsEntity
import com.example.engine.tools.JarvisToolEngine
import com.example.engine.tools.ToolExecutionResult
import kotlinx.coroutines.delay

data class AIProcessResult(
    val replyText: String,
    val toolResult: ToolExecutionResult? = null,
    val isDemoMode: Boolean = false
)

class JarvisAIEngine(
    private val repository: JarvisRepository,
    private val toolEngine: JarvisToolEngine
) {
    private val geminiClient = GeminiClient()

    suspend fun processUserMessage(
        userPrompt: String,
        imageBitmap: Bitmap? = null,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        onStreamChunk: (String) -> Unit
    ): AIProcessResult {
        val settings = repository.getUserSettingsDirect()

        // 1. Tool intent detection
        val toolResult = detectAndRunTools(userPrompt)

        // 2. Build system context including memories
        val systemPrompt = buildSystemPrompt(settings, toolResult)

        // 3. Check if we should use Demo Mode or Gemini API
        if (settings.isDemoMode) {
            val demoResponse = generateDemoResponse(userPrompt, toolResult, settings)
            // Stream chunks with realistic typewriter effect
            streamSimulatedText(demoResponse, onStreamChunk)
            return AIProcessResult(
                replyText = demoResponse,
                toolResult = toolResult,
                isDemoMode = true
            )
        }

        // Try Gemini API first
        try {
            val response = geminiClient.generateContentStream(
                modelName = settings.aiModel,
                prompt = if (toolResult != null) {
                    "$userPrompt\n\n[CONTEXT FROM EXECUTED TOOL '${toolResult.toolName}']:\n${toolResult.result}"
                } else userPrompt,
                systemInstruction = systemPrompt,
                bitmap = imageBitmap,
                history = conversationHistory.takeLast(10),
                onChunkReceived = onStreamChunk
            )

            if (response.isBlank()) {
                throw RuntimeException("Empty response from AI engine")
            }

            return AIProcessResult(
                replyText = response,
                toolResult = toolResult,
                isDemoMode = false
            )
        } catch (e: Exception) {
            // Fallback gracefully to offline intelligence with clear indicator
            val fallbackResponse = generateDemoResponse(userPrompt, toolResult, settings)
            val fullFallback = if (e.message == "API_KEY_NOT_CONFIGURED") {
                "$fallbackResponse\n\n> *[MODE DÉMO ACTIF — Clé API non configurée dans le panneau Secrets]*"
            } else {
                "$fallbackResponse\n\n> *[MODE SECOURS LOCAL — ${e.localizedMessage ?: "Connexion IA interrompue"}]*"
            }
            streamSimulatedText(fullFallback, onStreamChunk)
            return AIProcessResult(
                replyText = fullFallback,
                toolResult = toolResult,
                isDemoMode = true
            )
        }
    }

    private suspend fun buildSystemPrompt(
        settings: UserSettingsEntity,
        toolResult: ToolExecutionResult?
    ): String {
        val memories = if (settings.memoryEnabled) {
            repository.getActiveMemories()
        } else emptyList()

        val memoryContext = if (memories.isNotEmpty()) {
            "\n\nUSER PERSONAL MEMORY VAULT:\n" + memories.joinToString("\n") { "- ${it.key}: ${it.content}" }
        } else ""

        val toolContext = if (toolResult != null) {
            "\n\nAUTOMATED TOOL EXECUTION RESULT (${toolResult.toolName}):\n${toolResult.result}\n(Synthesize this tool output into your sophisticated, natural response.)"
        } else ""

        return """
            You are JARVIS (Just A Rather Very Intelligent System), a sophisticated, calm, and highly capable personal AI assistant.
            
            Personality Guidelines:
            - Professional, composed, polite, intelligent, with subtle high-tech wit.
            - Address user as '${settings.userName}' when appropriate.
            - Concise by default, deep and articulate when asked.
            - Never invent facts or hallucinate external capabilities.
            - Adapt naturally to the language of the prompt (French if French, English if English).
            - Use occasional sophisticated phrases such as "Bien sûr", "Compris", "Analyse terminée", "Voici ce que j'ai trouvé", "À vos ordres".
            $memoryContext
            $toolContext
        """.trimIndent()
    }

    private suspend fun detectAndRunTools(prompt: String): ToolExecutionResult? {
        val lower = prompt.lowercase().trim()

        // 1. Calculator
        if (Regex("(\\d+\\s*[+\\-*\\/xX×÷]\\s*\\d+)").containsMatchIn(prompt) ||
            lower.startsWith("combien font") || lower.startsWith("calcule") || lower.startsWith("calculate")
        ) {
            val expr = prompt.replace(Regex("(?i)(combien font|calcule|calculate|what is|equals)"), "").trim()
            if (expr.isNotEmpty()) {
                return toolEngine.executeTool("calculator", expr)
            }
        }

        // 2. Weather
        if (lower.contains("météo") || lower.contains("weather") || lower.contains("quel temps") || lower.contains("temperature")) {
            val location = lower
                .replace("météo", "")
                .replace("weather", "")
                .replace("quel temps fait-il à", "")
                .replace("quel temps fait-il en", "")
                .replace("aujourd'hui", "")
                .replace("today", "")
                .replace("à", "")
                .replace("in", "")
                .trim()
            return toolEngine.executeTool("weather", location.ifBlank { "Paris" })
        }

        // 3. World Time / Date
        if (lower.contains("quelle heure") || lower.contains("what time") || lower.contains("heure à") || lower.contains("time in") || lower.contains("date d'aujourd'hui")) {
            val city = lower
                .replace("quelle heure est-il à", "")
                .replace("quelle heure est-il en", "")
                .replace("what time is it in", "")
                .replace("heure à", "")
                .replace("time in", "")
                .trim()
            return toolEngine.executeTool("world_time", city)
        }

        // 4. System Diagnostics
        if (lower.contains("statut système") || lower.contains("system status") || lower.contains("diagnostique") || lower.contains("batterie") || lower.contains("battery") || lower.contains("télémétrie")) {
            return toolEngine.executeTool("system_status", "")
        }

        // 5. Notes
        if (lower.startsWith("note:") || lower.startsWith("créer une note") || lower.startsWith("ajoute une note") || lower.startsWith("prends note")) {
            val noteContent = prompt.replace(Regex("(?i)(note:|créer une note|ajoute une note|prends note)"), "").trim()
            return toolEngine.executeTool("notes_manager", "create: $noteContent")
        }

        // 6. Memory store
        if (lower.startsWith("rappelle-toi") || lower.startsWith("retiens que") || lower.startsWith("remember that") || lower.startsWith("enregistre dans ta mémoire")) {
            val fact = prompt.replace(Regex("(?i)(rappelle-toi que|rappelle-toi|retiens que|remember that|enregistre dans ta mémoire)"), "").trim()
            return toolEngine.executeTool("memory_vault", "save: $fact")
        }

        // 7. Web Search
        if (lower.startsWith("cherche") || lower.startsWith("recherche") || lower.startsWith("search") || lower.contains("dernières nouvelles") || lower.contains("actualités sur")) {
            val q = prompt.replace(Regex("(?i)(cherche|recherche|search|dernières nouvelles sur|actualités sur)"), "").trim()
            return toolEngine.executeTool("web_search", q)
        }

        return null
    }

    private fun generateDemoResponse(
        prompt: String,
        toolResult: ToolExecutionResult?,
        settings: UserSettingsEntity
    ): String {
        val lower = prompt.lowercase().trim()
        val user = settings.userName

        if (toolResult != null) {
            return when (toolResult.toolName) {
                "calculator" -> "Calcul terminé avec succès, $user.\n\n${toolResult.result}"
                "weather" -> "Voici les paramètres météorologiques actuels :\n\n${toolResult.result}\n\nL'atmosphère est stable. Souhaitez-vous d'autres relevés atmosphériques ?"
                "world_time" -> "Synchronisation temporelle effectuée :\n\n${toolResult.result}"
                "system_status" -> "Analyse diagnostique complète des sous-systèmes JARVIS :\n\n```telemetry\n${toolResult.result}\n```\n\nTous les systèmes sont opérationnels, $user."
                "notes_manager" -> "Opération sur les archives validée :\n\n${toolResult.result}"
                "memory_vault" -> "Information mémorisée dans vos archives sécurisées :\n\n${toolResult.result}"
                "web_search" -> "Recherche d'informations effectuée. Synthèse des résultats :\n\n${toolResult.result}"
                else -> "Opération outil exécutée :\n\n${toolResult.result}"
            }
        }

        return when {
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") || lower.contains("hey") ->
                "Bonjour $user. Tous les protocoles sont actifs et calibrés. Comment puis-je vous assister aujourd'hui ?"
            lower.contains("qui es-tu") || lower.contains("présente-toi") || lower.contains("who are you") ->
                "Je suis **JARVIS** (*Just A Rather Very Intelligent System*), votre assistant personnel de nouvelle génération. Je supervise vos communications, l'analyse multimodale, vos notes, votre mémoire long-terme et l'exécution d'outils analytiques avancés."
            lower.contains("merci") || lower.contains("thanks") ->
                "C'est un plaisir de vous être utile, $user. N'hésitez pas si vous avez besoin d'autres analyses."
            lower.contains("aide") || lower.contains("help") || lower.contains("que peux-tu faire") ->
                """
                Voici un aperçu de mes capacités opérationnelles, $user :
                
                - 🎙️ **Interaction Vocale** : Écoute en direct et synthèse vocale haute fidélité.
                - 🧠 **Mémoire Persistante** : Rétention contrôlable de vos préférences et directives.
                - 🧮 **Outils Intégrés** : Calculatrice, météo mondiale, horloge universelle, notes et diagnostics système.
                - 🌐 **Recherche & Connaissances** : Recherche d'informations et synthèse de données.
                - 👁️ **Vision Multimodale** : Analyse approfondie d'images et de schémas.
                
                Que souhaitez-vous explorer ?
                """.trimIndent()
            else ->
                "Analyse terminée, $user. J'ai traité votre requête concernant « $prompt ». L'ensemble des paramètres est nominal. Que souhaitez-vous que nous exécutions ensuite ?"
        }
    }

    private suspend fun streamSimulatedText(fullText: String, onChunkReceived: (String) -> Unit) {
        val words = fullText.split(" ")
        for (i in words.indices) {
            val chunk = if (i == words.size - 1) words[i] else words[i] + " "
            onChunkReceived(chunk)
            delay(28) // realistic typewriter stream speed
        }
    }
}
