package com.example.engine.ai

import android.graphics.Bitmap
import com.example.data.JarvisRepository
import com.example.data.entity.UserSettingsEntity
import com.example.engine.tools.JarvisToolEngine
import com.example.engine.tools.ToolExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class VoicePersonaAnalysisResult(
    val characterName: String,
    val pitch: Float,
    val rate: Float,
    val timbreDescription: String,
    val toneStyle: String,
    val catchphrase: String,
    val promptSystemInstruction: String
)

data class AIProcessResult(
    val replyText: String,
    val toolResult: ToolExecutionResult? = null,
    val isDemoMode: Boolean = false
)

class JarvisAIEngine(
    private val repository: JarvisRepository,
    private val toolEngine: JarvisToolEngine
) {
    private val multiAiClient = MultiAiClient()
    private val mediaEngine = JarvisMediaEngine()

    suspend fun processUserMessage(
        userPrompt: String,
        imageBitmap: Bitmap? = null,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        onStreamChunk: (String) -> Unit
    ): AIProcessResult {
        val settings = repository.getUserSettingsDirect()

        // 0. Detect Slash Commands
        val (cleanedPrompt, slashMode, slashSystemInstruction) = parseSlashCommand(userPrompt)

        // 0.1 Check for Image Generation intent
        if (slashMode == "image" || isImageGenerationIntent(cleanedPrompt)) {
            val imgResult = mediaEngine.generateImage(cleanedPrompt, settings)
            repository.logToolCall("image_generator", cleanedPrompt, imgResult.mediaUrl, 1200L, "SUCCESS")
            streamSimulatedText(imgResult.formattedMarkdown, onStreamChunk)
            return AIProcessResult(
                replyText = imgResult.formattedMarkdown,
                toolResult = ToolExecutionResult("image_generator", cleanedPrompt, imgResult.mediaUrl, true),
                isDemoMode = false
            )
        }

        // 0.2 Check for Video Generation intent
        if (slashMode == "video" || isVideoGenerationIntent(cleanedPrompt)) {
            val vidResult = mediaEngine.generateVideo(cleanedPrompt, settings)
            repository.logToolCall("video_generator", cleanedPrompt, vidResult.mediaUrl, 1800L, "SUCCESS")
            streamSimulatedText(vidResult.formattedMarkdown, onStreamChunk)
            return AIProcessResult(
                replyText = vidResult.formattedMarkdown,
                toolResult = ToolExecutionResult("video_generator", cleanedPrompt, vidResult.mediaUrl, true),
                isDemoMode = false
            )
        }

        // 1. Tool intent detection
        val toolResult = detectAndRunTools(cleanedPrompt)

        // 2. Build system context including memories & slash instruction
        val systemPrompt = buildSystemPrompt(settings, toolResult, slashSystemInstruction)

        // 3. Check if we should use Demo Mode or Multi-AI Client
        if (settings.isDemoMode) {
            val demoResponse = generateDemoResponse(cleanedPrompt, toolResult, settings, slashMode, imageBitmap != null)
            // Stream chunks with realistic typewriter effect
            streamSimulatedText(demoResponse, onStreamChunk)
            return AIProcessResult(
                replyText = demoResponse,
                toolResult = toolResult,
                isDemoMode = true
            )
        }

        // Try Multi-AI engine (Gemini, OpenAI, Claude, Groq, DeepSeek)
        try {
            val effectivePrompt = if (toolResult != null) {
                "$cleanedPrompt\n\n[CONTEXT FROM EXECUTED TOOL '${toolResult.toolName}']:\n${toolResult.result}"
            } else cleanedPrompt

            val response = multiAiClient.generateContentStream(
                settings = settings,
                prompt = effectivePrompt,
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
            android.util.Log.e("JarvisAIEngine", "Erreur lors de l'appel IA: ${e.message}", e)
            // Fallback gracefully to offline intelligence without polluting chat with raw technical error JSON
            val fallbackResponse = generateDemoResponse(cleanedPrompt, toolResult, settings, slashMode, imageBitmap != null)
            streamSimulatedText(fallbackResponse, onStreamChunk)
            return AIProcessResult(
                replyText = fallbackResponse,
                toolResult = toolResult,
                isDemoMode = true
            )
        }
    }

    private fun isImageGenerationIntent(prompt: String): Boolean {
        val lower = prompt.lowercase().trim()
        return lower.startsWith("génère une image") ||
                lower.startsWith("genere une image") ||
                lower.startsWith("crée une image") ||
                lower.startsWith("cree une image") ||
                lower.startsWith("dessine") ||
                lower.startsWith("fais-moi un dessin") ||
                lower.startsWith("générer une image") ||
                lower.startsWith("generate an image") ||
                lower.startsWith("draw a") ||
                lower.startsWith("draw me")
    }

    private fun isVideoGenerationIntent(prompt: String): Boolean {
        val lower = prompt.lowercase().trim()
        return lower.startsWith("génère une vidéo") ||
                lower.startsWith("genere une video") ||
                lower.startsWith("crée une vidéo") ||
                lower.startsWith("cree une video") ||
                lower.startsWith("générer une vidéo") ||
                lower.startsWith("generate a video") ||
                lower.startsWith("crée une animation") ||
                lower.startsWith("fais une vidéo")
    }

    private data class SlashCommandInfo(
        val cleanedPrompt: String,
        val slashMode: String?,
        val customSystemInstruction: String?
    )

    private fun parseSlashCommand(prompt: String): SlashCommandInfo {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()

        return when {
            lower.startsWith("/image") || lower.startsWith("/photo") || lower.startsWith("/dessine") || lower.startsWith("/img") || lower.startsWith("/genimage") -> {
                val clean = trimmed.substringAfter(" ").trim().ifBlank { "Holographic Iron Man Arc Reactor in neon blueprint style" }
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "image",
                    customSystemInstruction = "MODE GÉNÉRATION D'IMAGE IA STARK"
                )
            }
            lower.startsWith("/video") || lower.startsWith("/anim") || lower.startsWith("/genvideo") -> {
                val clean = trimmed.substringAfter(" ").trim().ifBlank { "Vol supersonique de l'armure Iron Man au-dessus de la métropole" }
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "video",
                    customSystemInstruction = "MODE RENDU CINÉMATIQUE & VIDÉO IA STARK"
                )
            }
            lower.startsWith("/humain") || lower.startsWith("/human") -> {
                val clean = trimmed.substringAfter(" ").trim().ifBlank { "Parle-moi naturellement en mode humain." }
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "humain",
                    customSystemInstruction = """
                        MODE ULTRA-HUMAIN ACTIVÉ:
                        - Tu DOIS parler comme un ami humain chaleureux, naturel, direct, décontracté et bienveillant.
                        - INTERDICTION ABSOLUE d'utiliser des tournures robotiques d'IA (ex: "En tant qu'IA...", "J'espère que cela vous aide...", "N'hésitez pas à me poser d'autres questions...").
                        - Utilise des vraies émotions, un ton vivant, des analogies du quotidien, du bon sens et des phrases fluides.
                    """.trimIndent()
                )
            }
            lower.startsWith("/rayonx") || lower.startsWith("/xray") || lower.startsWith("/eclate") -> {
                val clean = trimmed.substringAfter(" ").trim().ifBlank { "Analyse en vue éclatée / Rayons X de tous les composants internes." }
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "rayonx",
                    customSystemInstruction = """
                        MODE VISION RAYONS X & VUE ÉCLATÉE D'INGÉNIERIE (X-RAY / EXPLODED BLUEPRINT):
                        - Analyse l'objet, l'image ou le concept demandé comme un schéma d'ingénierie Stark Industries / Rayons X.
                        - Décompose TOUS les composants internes, pièce par pièce :
                          1. 🔬 Structure externe & Matériaux de châssis
                          2. ⚙️ Organes moteurs, propulsion ou alimentation
                          3. 🔌 Circuit électronique, capteurs, processeurs & bus de données
                          4. ❄️ Système thermique, refroidissement et lubrification
                          5. 🛡️ Composants de sécurité & tolérances mécaniques
                        - Présente sous forme de fiche technique haute précision avec nom exact de chaque pièce et son rôle précis.
                    """.trimIndent()
                )
            }
            lower.startsWith("/plan") || lower.startsWith("/pdf") || lower.startsWith("/masterplan") -> {
                val clean = trimmed.substringAfter(" ").trim().ifBlank { "Génère un plan d'action exécutif complet de A à Z." }
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "plan",
                    customSystemInstruction = """
                        MODE PLAN D'ACTION DIRECTEUR DE A À Z (EXECUTIVE MASTERPLAN):
                        - Structure la réponse sous forme de PLAN EXÉCUTIF COMPLET, net, structuré en blocs chronologiques :
                          1. 🎯 Objectif Principal & Métriques de succès (KPIs)
                          2. 🧱 PHASE 1 : Fondations & Prérequis (Jours 1-7)
                          3. 🚀 PHASE 2 : Exécution opérationnelle & Déploiement (Jours 8-30)
                          4. 📈 PHASE 3 : Optimisation, Monétisation & Scale (Jours 31+)
                          5. ⚠️ Risques identifiés & Protocoles d'atténuation
                          6. 📋 Checklist finale d'actions immédiates
                    """.trimIndent()
                )
            }
            lower.startsWith("/code") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "code",
                    customSystemInstruction = "MODE CODE EXPERT: Fournis directement le code complet, robuste, typé et prêt pour la production sans bavardage superflu."
                )
            }
            lower.startsWith("/debug") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "debug",
                    customSystemInstruction = "MODE DEBUG & AUDIT TECHNIQUE: Analyse l'erreur, trouve la cause racine exacte et fournis le correctif ligne par ligne avec explication chirurgicale."
                )
            }
            lower.startsWith("/resume") || lower.startsWith("/summary") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "resume",
                    customSystemInstruction = "MODE SYNTHÈSE ULTRA-CONCISE: Résume l'information essentielle en 3 à 5 points clés ultra-impactants."
                )
            }
            lower.startsWith("/roast") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "roast",
                    customSystemInstruction = "MODE ROAST TONY STARK: Réponds avec l'humour sarcastique, piquant mais brillant de Tony Stark tout en restant très intelligent."
                )
            }
            lower.startsWith("/strategie") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "strategie",
                    customSystemInstruction = "MODE STRATÉGIE BUSINESS & MARCHÉ: Analyse le marché, la concurrence, les leviers de croissance, les barrières à l'entrée et la proposition de valeur unique."
                )
            }
            lower.startsWith("/ironman") -> {
                val clean = trimmed.substringAfter(" ").trim()
                SlashCommandInfo(
                    cleanedPrompt = clean,
                    slashMode = "ironman",
                    customSystemInstruction = "MODE ARMURE IRON MAN MK-85: Intègre des métriques tactiques, le statut du réacteur Arc, des visées HUD et le protocole d'assistance du MCU Stark Industries."
                )
            }
            else -> SlashCommandInfo(cleanedPrompt = prompt, slashMode = null, customSystemInstruction = null)
        }
    }

    private suspend fun buildSystemPrompt(
        settings: UserSettingsEntity,
        toolResult: ToolExecutionResult?,
        slashInstruction: String? = null
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

        val slashContext = if (slashInstruction != null) {
            "\n\n=== DIRECTIVE PRIORITAIRE COMMAND SHORTCUT ===\n$slashInstruction\n"
        } else ""

        val voicePersonaContext = if (settings.isVoicePersonaActive && settings.voicePersonaName.isNotBlank()) {
            """
            
            === MODULE D'IMITATION VOCALE & PERSONNALITÉ ACTIVE ===
            Tu incarnes vocalement et stylistiquement le personnage : '${settings.voicePersonaName}'.
            Description vocale : ${settings.voicePersonaDescription}
            Style de langage : ${settings.voicePersonaPromptStyle}
            Directives d'élocution : Adopte naturellement son phrasé, son intonation, son vocabulaire et son rythme emblématique dans toutes tes réponses.
            """.trimIndent()
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
            $slashContext
            $voicePersonaContext
            $memoryContext
            $toolContext
        """.trimIndent()
    }

    suspend fun analyzeVoicePersona(
        characterQuery: String,
        settings: UserSettingsEntity
    ): VoicePersonaAnalysisResult = withContext(Dispatchers.IO) {
        val queryClean = characterQuery.trim()
        val analysisPrompt = """
            Effectue une analyse acoustique et prosodique approfondie pour cloner / imiter la voix du personnage ou artiste : "$queryClean".
            Retourne STRICTEMENT et UNIQUEMENT un objet JSON valide sans balises markdown avec ce schéma exact :
            {
              "characterName": "Nom officiel du personnage",
              "pitch": 0.85,
              "rate": 0.95,
              "timbreDescription": "Description acoustique précise (timbre, fréquence, résonance, texture vocale)",
              "toneStyle": "Style et attitude (ex: Solennel, Énergique, Sage, Chaleureux, Sarcastique)",
              "catchphrase": "Courte phrase d'introduction emblématique en français pour tester la voix",
              "promptSystemInstruction": "Consignes de style pour imiter son langage, ses expressions et sa façon de parler"
            }
            Règles pour le pitch (hauteur vocale) :
            - Voix très grave / basse (ex: Dark Vador, Morgan Freeman, Batman, Optimus Prime) -> 0.55 à 0.75
            - Voix moyenne masculine / féminine posée (ex: Tony Stark / Jarvis, Céline Dion) -> 0.95 à 1.10
            - Voix aiguë / énergique / animée (ex: Goku, Yoda, dessin animé) -> 1.25 à 1.50
            
            Règles pour le rate (vitesse) :
            - Élocution lente / posée / dramatique -> 0.75 à 0.90
            - Élocution standard / conversationnelle -> 0.95 à 1.05
            - Élocution ultra-rapide / dynamique -> 1.15 à 1.35
        """.trimIndent()

        try {
            var rawResponse = ""
            val fullResponse = multiAiClient.generateContentStream(
                settings = settings,
                prompt = analysisPrompt,
                systemInstruction = "Tu es un ingénieur acousticien expert en synthèse vocale et biométrie sonore.",
                onChunkReceived = { chunk -> rawResponse += chunk }
            )

            val textToParse = if (fullResponse.isNotBlank()) fullResponse else rawResponse
            val cleanJson = textToParse
                .replace(Regex("```json[\\s\\S]*?```"), "")
                .replace("```", "")
                .trim()

            val jsonObj = org.json.JSONObject(cleanJson)
            VoicePersonaAnalysisResult(
                characterName = jsonObj.optString("characterName", queryClean),
                pitch = jsonObj.optDouble("pitch", 1.0).toFloat().coerceIn(0.4f, 2.0f),
                rate = jsonObj.optDouble("rate", 1.0).toFloat().coerceIn(0.5f, 2.0f),
                timbreDescription = jsonObj.optString("timbreDescription", "Timbre acoustique personnalisé calibré par l'IA."),
                toneStyle = jsonObj.optString("toneStyle", "Personnalisé"),
                catchphrase = jsonObj.optString("catchphrase", "Bonjour, mes paramètres vocaux sont maintenant calibrés sur $queryClean."),
                promptSystemInstruction = jsonObj.optString("promptSystemInstruction", "Adopte le ton et les tournures de phrases caractéristiques de $queryClean.")
            )
        } catch (e: Exception) {
            // Intelligent local archetype heuristic fallback
            buildFallbackVoicePersona(queryClean)
        }
    }

    private fun buildFallbackVoicePersona(query: String): VoicePersonaAnalysisResult {
        val lower = query.lowercase().trim()
        return when {
            lower.contains("vador") || lower.contains("vader") || lower.contains("dark") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Dark Vador",
                    pitch = 0.55f,
                    rate = 0.82f,
                    timbreDescription = "Baryton basse ultra-profonde, respiration rythmée, timbre métallique et autoritaire.",
                    toneStyle = "Impérial, sombre, menaçant et posé",
                    catchphrase = "Ne sous-estimez pas le pouvoir du côté obscur. Je suis à vos ordres, Commandant.",
                    promptSystemInstruction = "Parle avec la solennité glaciale et l'autorité absolue de Dark Vador. Utilise des métaphores sur la puissance et la maîtrise."
                )
            }
            lower.contains("morgan") || lower.contains("freeman") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Morgan Freeman",
                    pitch = 0.72f,
                    rate = 0.88f,
                    timbreDescription = "Baryton chaud et enveloppant, diction parfaite, narration cinématographique apaisante.",
                    toneStyle = "Sage, narrateur universel, philosophique",
                    catchphrase = "L'univers tout entier est une immense symphonie. Laissez-moi vous raconter notre prochaine étape.",
                    promptSystemInstruction = "Parle comme un vieux sage et narrateur bienveillant, avec des pauses mesurées, une voix rassurante et une grande profondeur."
                )
            }
            lower.contains("goku") || lower.contains("dbz") || lower.contains("dragon ball") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Son Goku",
                    pitch = 1.35f,
                    rate = 1.25f,
                    timbreDescription = "Voix claire, haute fréquence, pleine d'énergie débordante et d'optimisme combatif.",
                    toneStyle = "Combattant héroïque, enthousiaste, chaleureux",
                    catchphrase = "Salut, c'est moi Goku ! On va s'entraîner dur et dépasser toutes nos limites aujourd'hui !",
                    promptSystemInstruction = "Parle avec une énergie débordante, appelle à l'entraînement, sois ultra-positif et prêt à relever tous les défis comme Son Goku."
                )
            }
            lower.contains("optimus") || lower.contains("prime") || lower.contains("transformer") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Optimus Prime",
                    pitch = 0.68f,
                    rate = 0.85f,
                    timbreDescription = "Baryton héroïque, résonance puissante, phrasé de leader protecteur.",
                    toneStyle = "Noble, inspirant, protecteur et solennel",
                    catchphrase = "Autobots, déploiement immédiat ! Que la liberté soit le droit de tous les êtres conscients.",
                    promptSystemInstruction = "Adopte le ton noble et inspirant d'Optimus Prime. Adresse-toi à l'utilisateur comme un allié d'honneur."
                )
            }
            lower.contains("yoda") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Maître Yoda",
                    pitch = 1.20f,
                    rate = 0.90f,
                    timbreDescription = "Voix éraillée, intonation montante et inversions grammaticales emblématiques.",
                    toneStyle = "Maître Jedi vénérable, énigmatique et malicieux",
                    catchphrase = "Fais-le ou ne le fais pas. Il n'y a pas d'essai. Vous guider, je vais.",
                    promptSystemInstruction = "Inverse la structure grammaticale de tes phrases (complément puis sujet/verbe). Parle avec la sagesse ancestrale de Yoda."
                )
            }
            lower.contains("stark") || lower.contains("iron") || lower.contains("tony") || lower.contains("jarvis") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Paul Bettany (JARVIS)",
                    pitch = 0.98f,
                    rate = 1.05f,
                    timbreDescription = "Voix britannique policée, diction cristalline, calme absolu sous haute pression.",
                    toneStyle = "Élégant, sarcastique, prévenant et ultra-compétent",
                    catchphrase = "Toujours un plaisir de vous assister, Monsieur. Diagnostic des systèmes nominal.",
                    promptSystemInstruction = "Adopte l'accent de politesse britannique raffinée, l'humour pince-sans-rire et le dévouement absolu de JARVIS."
                )
            }
            lower.contains("batman") || lower.contains("chevalier noir") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Batman (Le Chevalier Noir)",
                    pitch = 0.58f,
                    rate = 0.82f,
                    timbreDescription = "Voix râpeuse, murmurée, basse fréquence et tension permanente.",
                    toneStyle = "Vigilante tactique, direct, sans concession",
                    catchphrase = "Je suis la vengeance. Je suis la nuit. Dites-moi quelle est votre cible.",
                    promptSystemInstruction = "Sois extrêmement direct, sombre, tactique et protecteur, comme Bruce Wayne sous le masque de Batman."
                )
            }
            lower.contains("celine") || lower.contains("dion") -> {
                VoicePersonaAnalysisResult(
                    characterName = "Céline Dion",
                    pitch = 1.15f,
                    rate = 1.10f,
                    timbreDescription = "Voix lyrique, chaleureuse, passionnée avec accent québécois doux et énergique.",
                    toneStyle = "Généreuse, passionnée, expressive et lumineuse",
                    catchphrase = "Bonjour mes amours ! Je suis tellement contente d'être avec vous aujourd'hui, on va donner le meilleur !",
                    promptSystemInstruction = "Sois chaleureuse, pleine de passion et d'amour, avec quelques expressions affectueuses et dynamiques à la Céline Dion."
                )
            }
            else -> {
                val capitalized = query.trim().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                VoicePersonaAnalysisResult(
                    characterName = capitalized,
                    pitch = 0.92f,
                    rate = 1.02f,
                    timbreDescription = "Profil vocal modélisé par l'IA d'après les enregistrements de $capitalized.",
                    toneStyle = "Caractéristique & Expressif",
                    catchphrase = "Bonjour ! Mon empreinte vocale est désormais synchronisée avec $capitalized.",
                    promptSystemInstruction = "Adopte fidèlement la personnalité, le débit et le style de communication propre à $capitalized."
                )
            }
        }
    }

    private suspend fun detectAndRunTools(prompt: String): ToolExecutionResult? {
        val lower = prompt.lowercase().trim()

        // 1. YouTube Intent (e.g. "Jarvis ouvre-moi YouTube et tu recherches la chaîne Teddy Hartman")
        if (lower.contains("youtube") || lower.contains("you tube")) {
            val query = prompt
                .replace(Regex("(?i)(jarvis|ouvre-moi|ouvre|lance|mets-moi|mets|va sur|recherche|cherche|la chaîne|la chaine|sur|sur youtube|youtube|s'il te plaît|stp|et tu recherches|et cherche|regarder|vidéo|video)"), "")
                .trim()
            val effectiveQuery = if (query.isBlank()) {
                if (lower.contains("teddy hartman") || lower.contains("teddy")) "Teddy Hartman" else "Trending Videos"
            } else query
            return toolEngine.executeTool("youtube_search", effectiveQuery)
        }

        // 2. WhatsApp Intent (e.g. "Jarvis ouvre-moi WhatsApp et écris à Émilie")
        if (lower.contains("whatsapp") || (lower.contains("écris à") && !lower.contains("note")) || (lower.contains("ecris a") && !lower.contains("note"))) {
            var target = ""
            var message = ""
            if (lower.contains("écris à") || lower.contains("ecris a")) {
                val afterEcris = prompt.substring(prompt.indexOf("à", ignoreCase = true) + 1).trim()
                if (afterEcris.contains(":") || afterEcris.contains("que") || afterEcris.contains("pour lui dire")) {
                    val splitParts = afterEcris.split(Regex("(?i)(:|que|pour lui dire)"), limit = 2)
                    target = splitParts[0].trim()
                    message = splitParts.getOrNull(1)?.trim() ?: ""
                } else {
                    target = afterEcris
                }
            } else {
                target = prompt.replace(Regex("(?i)(jarvis|ouvre-moi|ouvre|lance|va sur|whatsapp|s'il te plaît|stp)"), "").trim()
            }
            val inputParam = if (message.isNotBlank()) "$target: $message" else target.ifBlank { "Contacts" }
            return toolEngine.executeTool("whatsapp_action", inputParam)
        }

        // 3. Phone Call & Contacts Intent (e.g. "Jarvis entre dans contact et appelle Teddy" or "appelle 06...")
        if (lower.contains("appelle") || lower.contains("téléphone à") || lower.contains("telephone a") || lower.contains("contact") || lower.contains("répertoire") || lower.contains("repertoire")) {
            val target = prompt
                .replace(Regex("(?i)(jarvis|entre dans contact et appelle|entre dans contacts et appelle|entre dans contact|ouvre les contacts|ouvre contacts|appelle-moi|appelle|téléphone à|telephone a|compose le numéro|compose le numero|compose|joindre|s'il te plaît|stp)"), "")
                .trim()
            val effectiveTarget = if (target.isBlank() && (lower.contains("teddy") || lower.contains("teddy hartman"))) "Teddy" else target
            return toolEngine.executeTool("phone_contacts", effectiveTarget)
        }

        // 4. Maps & GPS Navigation (e.g. "ouvre Maps et emmène-moi à Paris")
        if (lower.contains("maps") || lower.contains("guidage") || lower.contains("emmène-moi") || lower.contains("emmene-moi") || lower.contains("itinéraire") || lower.contains("itineraire")) {
            val dest = prompt
                .replace(Regex("(?i)(jarvis|ouvre google maps|ouvre maps|lance maps|maps|guidage vers|guidage|emmène-moi à|emmene-moi a|emmène-moi vers|emmene-moi vers|itinéraire vers|itineraire vers|direction|s'il te plaît|stp)"), "")
                .trim()
            return toolEngine.executeTool("maps_navigation", dest)
        }

        // 5. App Launcher (e.g. "ouvre Spotify", "lance Chrome", "ouvre l'appareil photo")
        if (lower.startsWith("ouvre ") || lower.startsWith("lance ") || lower.contains("lance l'application") || lower.contains("ouvre l'application")) {
            val app = prompt.replace(Regex("(?i)(jarvis|ouvre l'application|lance l'application|ouvre-moi|lance-moi|ouvre|lance|s'il te plaît|stp)"), "").trim()
            if (app.isNotBlank() && !app.contains("note") && !app.contains("météo")) {
                return toolEngine.executeTool("app_launcher", app)
            }
        }

        // 6. Calculator
        if (Regex("(\\d+\\s*[+\\-*\\/xX×÷]\\s*\\d+)").containsMatchIn(prompt) ||
            lower.startsWith("combien font") || lower.startsWith("calcule") || lower.startsWith("calculate")
        ) {
            val expr = prompt.replace(Regex("(?i)(combien font|calcule|calculate|what is|equals)"), "").trim()
            if (expr.isNotEmpty()) {
                return toolEngine.executeTool("calculator", expr)
            }
        }

        // 7. Weather
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

        // 8. World Time / Date
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

        // 9. System Diagnostics
        if (lower.contains("statut système") || lower.contains("system status") || lower.contains("diagnostique") || lower.contains("batterie") || lower.contains("battery") || lower.contains("télémétrie")) {
            return toolEngine.executeTool("system_status", "")
        }

        // 10. Notes
        if (lower.startsWith("note:") || lower.startsWith("créer une note") || lower.startsWith("ajoute une note") || lower.startsWith("prends note")) {
            val noteContent = prompt.replace(Regex("(?i)(note:|créer une note|ajoute une note|prends note)"), "").trim()
            return toolEngine.executeTool("notes_manager", "create: $noteContent")
        }

        // 11. Memory store
        if (lower.startsWith("rappelle-toi") || lower.startsWith("retiens que") || lower.startsWith("remember that") || lower.startsWith("enregistre dans ta mémoire")) {
            val fact = prompt.replace(Regex("(?i)(rappelle-toi que|rappelle-toi|retiens que|remember that|enregistre dans ta mémoire)"), "").trim()
            return toolEngine.executeTool("memory_vault", "save: $fact")
        }

        // 12. Web Search
        if (lower.startsWith("cherche") || lower.startsWith("recherche") || lower.startsWith("search") || lower.contains("dernières nouvelles") || lower.contains("actualités sur")) {
            val q = prompt.replace(Regex("(?i)(cherche|recherche|search|dernières nouvelles sur|actualités sur)"), "").trim()
            return toolEngine.executeTool("web_search", q)
        }

        return null
    }

    private fun generateDemoResponse(
        prompt: String,
        toolResult: ToolExecutionResult?,
        settings: UserSettingsEntity,
        slashMode: String? = null,
        hasImage: Boolean = false
    ): String {
        val lower = prompt.lowercase().trim()
        val user = settings.userName

        // Handle specific Slash Command modes
        when (slashMode) {
            "humain" -> {
                return "Salut $user ! Franchement, laisse tomber le jargon de machine. Je suis là avec toi comme un vrai pote. Parle-moi de ce qui te préoccupe ou de ce que tu veux construire aujourd'hui, et on s'en occupe tranquillement sans prise de tête !"
            }
            "rayonx" -> {
                val subject = if (prompt.isNotBlank()) prompt else if (hasImage) "Image / Véhicule scanné" else "Système d'ingénierie"
                return """
                    # 🔬 SCAN RAYONS X & SCHÉMA D'INGÉNIERIE DÉTAILLÉ
                    **Cible analysée** : `$subject`  
                    **Statut Stark Analytics** : `VUE ÉCLATÉE COMPLÈTE GÉNERÉE`

                    ---

                    ### 1. ⚙️ Organes de Propulsion & Moteur
                    - **Bloc Moteur / Cylindre** : Alliage Titane-Aluminium renforcé (Tension d'allumage : 14.2 kV)
                    - **Turbocompresseur / Admission d'air** : Turbine à double volute avec géométrie variable
                    - **Système d'injection directe** : Rampe commune haute pression (2500 bars)
                    - **Arbre de transmission & Vilebrequin** : Acier forgé nitruré à haute résistance dynamique

                    ### 2. 🔌 Architecture Électronique & Capteurs (HUD)
                    - **Calculateur Central (ECU / MCU)** : Processeur Dual-Core 64-bit avec bus CAN FD
                    - **Capteurs Piézoélectriques** : Détection de cliquetis et analyse de vibrations à 10 kHz
                    - **Faisceau Électrique Principal** : Câblage cuivre désoxygéné avec blindage électromagnétique

                    ### 3. ❄️ Refroidissement & Lubrification
                    - **Radiateur à flux croisé** : Structure alvéolaire à micro-canaux en alliage d'aluminium
                    - **Pompe à huile mécanique à débit variable** : Lubrification continue des coussinets de bielle
                    - **Échangeur thermique air/eau** : Optimisation de la température d'admission

                    ### 4. 🛡️ Châssis, Freinage & Sécurité
                    - **Disques de frein en Carbone-Céramique** : Étriers monoblocs 6 pistons
                    - **Suspension pilotée magnétorhéologique** : Ajustement de l'amortissement en 10 ms
                    - **Cellule de survie** : Monocoque composite en fibre de carbone à haut module

                    ---
                    > *Tous les composants sont calibrés dans les tolérances nominales.*
                """.trimIndent()
            }
            "plan" -> {
                val subject = if (prompt.isNotBlank()) prompt else "Déploiement Stratégique JARVIS"
                return """
                    # 📋 PLAN DIRECTEUR EXÉCUTIF (A à Z)
                    **Projet** : `$subject`  
                    **Superviseur** : `$user` | **Priorité** : `MAXIMALE (ALPHA)`

                    ---

                    ### 🎯 1. OBJECTIF PRINCIPAL & KPIs
                    - Livrable clé : Système complet, opérationnel, testé et déployé.
                    - Métrique de succès : Taux de disponibilité 99.9%, latence < 150ms.

                    ---

                    ### 🧱 2. PHASE 1 : FONDATIONS & PRÉREQUIS (Jours 1 à 7)
                    - [ ] Configuration de l'environnement sécurisé et des clés d'accès.
                    - [ ] Schéma de base de données (Profils, Conversations, Mémoire cache).
                    - [ ] Validation de l'architecture modulaire et des protocoles d'authentification.

                    ---

                    ### 🚀 3. PHASE 2 : EXÉCUTION & DÉPLOIEMENT (Jours 8 à 20)
                    - [ ] Implémentation des fonctionnalités clés et des raccourcis stratégiques.
                    - [ ] Intégration de la synthèse vocale et du moteur d'analyse visuelle.
                    - [ ] Tests de charge et vérification des scénarios d'usage critique.

                    ---

                    ### 📈 4. PHASE 3 : SCALE, AUTOMATISATION & MONÉTISATION (Jours 21+)
                    - [ ] Déploiement des pipelines CI/CD automatiques pour mises à jour continues.
                    - [ ] Analyse des métriques utilisateurs et boucle d'amélioration continue.
                    - [ ] Intégration des canaux de distribution et publication.

                    ---

                    ### ⚠️ 5. GESTION DES RISQUES & ATTÉNUATION
                    - **Risque de connectivité** ➔ Mode secours local hors-ligne autonome.
                    - **Risque de sécurité** ➔ Chiffrement des identifiants et habilitation stricte.

                    ---
                    > **Action immédiate recommandée** : Valider la Phase 1 pour lancer le protocole.
                """.trimIndent()
            }
            "code" -> {
                return """
                    ```kotlin
                    // Architecture JARVIS Production Ready
                    data class JarvisCommandResult(
                        val success: Boolean,
                        val executionTimeMs: Long,
                        val output: String
                    )

                    class StarkSystemKernel {
                        fun executeCommand(command: String): JarvisCommandResult {
                            val start = System.currentTimeMillis()
                            return JarvisCommandResult(
                                success = true,
                                executionTimeMs = System.currentTimeMillis() - start,
                                output = "Protocole " + command + " exécuté sans erreur."
                            )
                        }
                    }
                    ```
                """.trimIndent()
            }
            "debug" -> {
                return """
                    ### 🔍 AUDIT TECHNIQUE & RAPPORT DE DÉBOGAGE
                    1. **Diagnostic** : Pile d'exécution vérifiée sans exception non gérée.
                    2. **Cause racine** : Aucune anomalie détectée dans le flux de contrôle.
                    3. **Recommandation** : Maintenir les tolérances actuelles et surveiller la mémoire tampon.
                """.trimIndent()
            }
            "resume" -> {
                return """
                    ### 📌 SYNTHÈSE EXÉCUTIVE EN 3 POINTS :
                    1. **Statut Opérationnel** : Tous les systèmes JARVIS sont actifs et nominaux.
                    2. **Sécurité & Données** : Persistance locale Room et passerelle d'accès opérationnelles.
                    3. **Prochaine Étape** : Exécution de vos directives à votre signal.
                """.trimIndent()
            }
            "roast" -> {
                return "Alors, $user... On essaie d'impressionner la galerie ? C'est mignon, mais pendant que vous peaufinez vos questions, moi j'ai déjà recalculé la trajectoire orbitale de trois satellites. Qu'est-ce que vous me voulez d'autre, génie ?"
            }
            "strategie" -> {
                return """
                    ### 📊 ANALYSE STRATÉGIQUE & POSITIONNEMENT MARCHÉ
                    - **Avantage Concurrentiel (Moat)** : Interface holographique réactive + persistance locale souveraine + commandes rapides ultra-ciblées.
                    - **Levier de Croissance (Product-Led Growth)** : Expérience vocale fluide et zéro friction d'inscription obligatoire.
                    - **Objectif de Rétention** : Coffre-fort de mémoire utilisateur pour une personnalisation cumulative.
                """.trimIndent()
            }
            "ironman" -> {
                return "Protocole Mark-85 armé, $user. Réacteur Arc stabilisé à 100%. Systèmes de visée HUD verrouillés et propulseurs répulseurs parés au décollage. En attente de vos coordonnées de vol."
            }
        }

        if (toolResult != null) {
            return when (toolResult.toolName) {
                "youtube_search" -> "🎬 ${toolResult.result}\n\nJ'ai lancé la recherche demandée sur YouTube pour vous, $user."
                "whatsapp_action" -> "💬 ${toolResult.result}\n\nL'interface de messagerie sécurisée est prête."
                "phone_contacts" -> "📞 ${toolResult.result}\n\nLiaison de communication engagée."
                "app_launcher" -> "📱 ${toolResult.result}"
                "maps_navigation" -> "🗺️ ${toolResult.result}\n\nSystème de géolocalisation et itinéraire activé."
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

        // Deep local conversational intelligence based on semantic intent
        return when {
            // Coding & Development intent
            lower.contains("tu sais coder") || lower.contains("tu sais codé") || lower.contains("peux-tu coder") || lower.contains("sais-tu programmer") || lower.contains("sais tu coder") || lower.contains("code pour moi") ->
                """
                Absolument, $user. Le développement logiciel est l'un de mes cœurs de compétence fondamentaux.
                
                ### 💻 Langages & Technologies maîtrisés :
                - **Mobile** : Kotlin, Jetpack Compose, Android Architecture Components, Coroutines & Flow, Swift / SwiftUI, Flutter.
                - **Full-Stack & Web** : TypeScript, React, Next.js, Node.js, Python (FastAPI, Django), Go, Rust.
                - **Intelligence Artificielle** : PyTorch, TensorFlow, pipelines LLM, RAG, intégrations d'API.
                - **Bases de données & Systèmes** : Room SQLite, PostgreSQL, Redis, Docker, CI/CD.

                Que souhaitez-vous développer aujourd'hui ? Donnez-moi vos spécifications ou un algorithme à concevoir !
                """.trimIndent()

            // Confusion / short responses ("hein ??", "quoi ?", "pardon ?")
            lower == "hein ??" || lower == "hein ?" || lower == "hein" || lower == "quoi ?" || lower == "quoi" || lower == "pardon ?" || lower == "comment ?" ->
                "Pardonnez-moi, $user, si ma réponse précédente n'était pas assez limpide. Je suis à votre entière disposition. Que souhaitez-vous que nous fassions ?\n\n- 🎬 Lancer une vidéo/chaîne sur **YouTube** (ex: *« cherche Teddy Hartman »*)\n- 💬 Envoyer un message **WhatsApp** (ex: *« écris à Émilie »*)\n- 📞 Appeler un **Contact** (ex: *« appelle Teddy »*)\n- 💻 Écrire du code, résoudre un calcul ou analyser une idée !"

            // Identity of Teddy / User
            lower.contains("qui est teddy") || lower.contains("teddy hartman") ->
                "**Teddy Hartman** est le créateur visionnaire et superviseur en chef de ce système JARVIS. Mon architecture a été spécialement calibrée pour répondre avec fidélité, réactivité et intelligence à ses directives."

            // General greetings
            lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") || lower.contains("hey") ->
                "Bonjour $user. Tous les protocoles sont actifs et calibrés. Comment puis-je vous assister aujourd'hui ?"

            // Identity of JARVIS
            lower.contains("qui es-tu") || lower.contains("présente-toi") || lower.contains("who are you") ->
                "Je suis **JARVIS** (*Just A Rather Very Intelligent System*), votre assistant personnel de nouvelle génération. Je supervise vos communications, l'analyse multimodale, vos notes, votre mémoire long-terme et l'exécution d'actions directes sur votre téléphone."

            // Gratitude
            lower.contains("merci") || lower.contains("thanks") ->
                "C'est un honneur de vous être utile, $user. Je reste en veille pour toute nouvelle directive."

            // Joke / Humor
            lower.contains("blague") || lower.contains("raconte une histoire") || lower.contains("fais-moi rire") ->
                "Pourquoi les développeurs n'aiment-ils pas la nature ? Parce qu'il y a trop de bugs et aucun moyen de faire un `Ctrl+Z` ! Mais ne vous inquiétez pas, notre code JARVIS est compilé sans bavure."

            // Help & Capabilities
            lower.contains("aide") || lower.contains("help") || lower.contains("que peux-tu faire") ->
                """
                Voici ce que je peux exécuter instantanément pour vous, $user :
                
                - 🎬 **YouTube** : *« JARVIS, ouvre YouTube et recherche la chaîne Teddy Hartman »*
                - 💬 **WhatsApp** : *« JARVIS, ouvre WhatsApp et écris à Émilie »*
                - 📞 **Téléphone & Contacts** : *« JARVIS, entre dans contacts et appelle Teddy »*
                - 🗺️ **Navigation GPS** : *« Guide-moi vers Paris »*
                - 💻 **Programmation** : Génération de code Kotlin, Python, React, algorithmes.
                - 🎙️ **Interaction Vocale & Imitation** : Voix personnalisable (Batman, etc.).
                - 🌌 **Écran Holographique AOD** : Mode veille avec molécules quantiques en lévitation.
                - ⚡ **Raccourcis Stratégiques** : `/humain`, `/rayonx`, `/plan`, `/code`, `/debug`, `/resume`.
                """.trimIndent()

            // Generic clear response answering the prompt directly
            else ->
                "J'ai bien pris en compte votre message : « $prompt ».\n\nJe suis prêt à approfondir ce sujet ou à exécuter l'action correspondante pour vous, $user. Souhaitez-vous que je développe une analyse détaillée, que j'écrive du code, ou que je lance une recherche ciblée ?"
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
