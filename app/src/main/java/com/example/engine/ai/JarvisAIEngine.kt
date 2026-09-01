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
            // Fallback gracefully to offline intelligence with clear indicator
            val fallbackResponse = generateDemoResponse(cleanedPrompt, toolResult, settings, slashMode, imageBitmap != null)
            val fullFallback = if (e.message == "API_KEY_NOT_CONFIGURED") {
                "$fallbackResponse\n\n> *[MODE DÉMO ACTIF — Clé API non configurée dans le panneau Secrets ou Paramètres]*"
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
                - ⚡ **Raccourcis Stratégiques** : `/humain`, `/rayonx`, `/plan`, `/code`, `/debug`, `/resume`, `/roast`, `/strategie`, `/ironman`.
                - 🧠 **Mémoire Persistante** : Rétention contrôlable de vos préférences et directives.
                - 🧮 **Outils Intégrés** : Calculatrice, météo mondiale, horloge universelle, notes et diagnostics système.
                - 👁️ **Vision Multimodale** : Analyse approfondie d'images et schémas techniques en vue éclatée.
                
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
