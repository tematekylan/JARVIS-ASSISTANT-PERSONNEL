package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.JarvisRepository
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MemoryEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NoteEntity
import com.example.data.entity.ToolLogEntity
import com.example.data.entity.UserSettingsEntity
import com.example.engine.ai.JarvisAIEngine
import com.example.engine.tools.JarvisToolEngine
import com.example.engine.voice.JarvisVoiceEngine
import com.example.ui.components.JarvisCoreState
import com.example.ui.components.JarvisScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = JarvisRepository(db)
    val toolEngine = JarvisToolEngine(application, repository)
    val aiEngine = JarvisAIEngine(repository, toolEngine)
    val voiceEngine = JarvisVoiceEngine(application)
    val incidentEngine = com.example.engine.incident.JarvisIncidentEngine(application, repository)

    private val _currentScreen = MutableStateFlow(JarvisScreen.CHAT)
    val currentScreen: StateFlow<JarvisScreen> = _currentScreen.asStateFlow()

    private val _coreState = MutableStateFlow(JarvisCoreState.IDLE)
    val coreState: StateFlow<JarvisCoreState> = _coreState.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    // Flows from DB
    val conversations = repository.allConversations.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val memories = repository.allMemories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notes = repository.allNotes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val incidents = repository.allIncidents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recentToolLogs = repository.recentToolLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val totalToolCallsCount = repository.totalToolCallsCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val totalMessagesCount = repository.totalMessageCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val userSettings = repository.userSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val audioAmplitude = voiceEngine.audioAmplitude
    val recognizedVoiceText = voiceEngine.recognizedText

    private var messageCollectionJob: Job? = null
    private var aiProcessingJob: Job? = null

    init {
        // Initialize settings if null
        viewModelScope.launch {
            repository.getUserSettingsDirect()
        }

        // Voice Engine callbacks
        voiceEngine.onSpeechComplete = { spokenText ->
            sendUserMessage(spokenText, null)
        }

        voiceEngine.onSpeechError = { _ ->
            _coreState.value = JarvisCoreState.IDLE
        }

        // Observe voice speaking
        viewModelScope.launch {
            voiceEngine.isSpeaking.collectLatest { speaking ->
                if (speaking) {
                    _coreState.value = JarvisCoreState.SPEAKING
                } else if (_coreState.value == JarvisCoreState.SPEAKING) {
                    _coreState.value = JarvisCoreState.IDLE
                }
            }
        }

        // Observe voice listening
        viewModelScope.launch {
            voiceEngine.isListening.collectLatest { listening ->
                if (listening) {
                    _coreState.value = JarvisCoreState.LISTENING
                } else if (_coreState.value == JarvisCoreState.LISTENING) {
                    if (!_isStreaming.value && !voiceEngine.isSpeaking.value) {
                        _coreState.value = JarvisCoreState.IDLE
                    }
                }
            }
        }

        // Auto-select or create first conversation
        viewModelScope.launch {
            conversations.collectLatest { list ->
                if (_currentConversation.value == null) {
                    if (list.isNotEmpty()) {
                        selectConversation(list.first().id)
                    } else {
                        startNewConversation()
                    }
                }
            }
        }
    }

    fun navigateTo(screen: JarvisScreen) {
        _currentScreen.value = screen
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val id = repository.createConversation("Nouvelle session")
            selectConversation(id)
        }
    }

    fun selectConversation(id: Long) {
        viewModelScope.launch {
            val conv = repository.getConversation(id)
            _currentConversation.value = conv

            messageCollectionJob?.cancel()
            messageCollectionJob = launch {
                repository.getMessages(id).collectLatest { msgList ->
                    _messages.value = msgList
                }
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversation.value?.id == id) {
                _currentConversation.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun sendUserMessage(text: String, bitmap: Bitmap?, imageUri: String? = null) {
        if (text.isBlank() && bitmap == null) return

        viewModelScope.launch {
            var conv = _currentConversation.value
            if (conv == null) {
                val title = if (text.length > 25) text.take(25) + "..." else text.ifBlank { "Session JARVIS" }
                val newId = repository.createConversation(title)
                conv = repository.getConversation(newId)
                _currentConversation.value = conv
                selectConversation(newId)
            } else if (_messages.value.isEmpty()) {
                // Update title based on first query
                val title = if (text.length > 25) text.take(25) + "..." else text.ifBlank { "Session JARVIS" }
                repository.updateConversation(conv.copy(title = title))
            }

            val convId = conv!!.id

            // Save user message
            val userMsg = MessageEntity(
                conversationId = convId,
                role = "user",
                content = text,
                imageUri = imageUri,
                timestamp = System.currentTimeMillis()
            )
            repository.addMessage(userMsg)

            // Start AI processing
            _coreState.value = JarvisCoreState.THINKING
            _isStreaming.value = true
            _streamingResponse.value = ""

            aiProcessingJob?.cancel()
            aiProcessingJob = launch {
                try {
                    val history = _messages.value.takeLast(10).map { it.role to it.content }
                    val result = aiEngine.processUserMessage(
                        userPrompt = text,
                        imageBitmap = bitmap,
                        conversationHistory = history,
                        onStreamChunk = { chunk ->
                            _streamingResponse.value += chunk
                        }
                    )

                    // Insert final assistant message into DB
                    val assistantMsg = MessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = result.replyText,
                        toolName = result.toolResult?.toolName,
                        toolInput = result.toolResult?.input,
                        toolOutput = result.toolResult?.result,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.addMessage(assistantMsg)

                    _isStreaming.value = false
                    _streamingResponse.value = ""

                    // Check auto-speak setting
                    val settings = repository.getUserSettingsDirect()
                    if (settings.autoSpeakResponses) {
                        speakText(result.replyText)
                    } else {
                        _coreState.value = JarvisCoreState.IDLE
                    }
                } catch (e: Exception) {
                    _isStreaming.value = false
                    _streamingResponse.value = ""
                    _coreState.value = JarvisCoreState.ERROR

                    val settings = repository.getUserSettingsDirect()
                    val incident = incidentEngine.handleIncident(
                        throwable = e,
                        contextInfo = "Requête : $text",
                        settings = settings
                    )

                    val targetEmail = settings.developerAlertEmail.ifBlank { "temateteddy@gmail.com" }
                    val cleanErrorMessage = "J'ai rencontré une légère anomalie technique lors de l'exécution.\n\n" +
                            "🚨 **Incident #${incident.incidentCode} enregistré**\n" +
                            "📧 Diagnostic et code d'erreur transmis au développeur : `$targetEmail`\n" +
                            "🤖 **Le Collège d'IA de résolution** a été réuni en urgence pour analyser la cause racine et préparer le patch de correction.\n\n" +
                            "Consultez les détails et délibérations dans le **Command Center**."

                    val errMsg = MessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = cleanErrorMessage,
                        isError = true,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.addMessage(errMsg)
                }
            }
        }
    }

    fun startListening() {
        voiceEngine.startListening()
    }

    fun stopListening() {
        voiceEngine.stopListening()
        if (_coreState.value == JarvisCoreState.LISTENING) {
            _coreState.value = JarvisCoreState.IDLE
        }
    }

    private val _isAnalyzingVoice = MutableStateFlow(false)
    val isAnalyzingVoice: StateFlow<Boolean> = _isAnalyzingVoice.asStateFlow()

    fun speakText(text: String) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect()
            val (rate, pitch) = if (settings.isVoicePersonaActive && settings.voicePersonaName.isNotBlank()) {
                settings.voicePersonaRate to settings.voicePersonaPitch
            } else {
                settings.speechRate to settings.speechPitch
            }
            voiceEngine.setSpeechParameters(
                rate = rate,
                pitch = pitch,
                languageCode = settings.voiceLanguage
            )
            voiceEngine.speak(text)
        }
    }

    fun analyzeAndCloneVoiceCharacter(characterQuery: String, onResult: (Boolean, String) -> Unit) {
        val query = characterQuery.trim()
        if (query.isBlank()) {
            onResult(false, "Veuillez spécifier le nom d'un personnage, d'un artiste ou d'une célébrité.")
            return
        }

        viewModelScope.launch {
            _isAnalyzingVoice.value = true
            try {
                val currentSettings = repository.getUserSettingsDirect()
                val profile = aiEngine.analyzeVoicePersona(query, currentSettings)

                val updatedSettings = currentSettings.copy(
                    voicePersonaName = profile.characterName,
                    voicePersonaDescription = profile.timbreDescription,
                    voicePersonaPitch = profile.pitch,
                    voicePersonaRate = profile.rate,
                    voicePersonaPromptStyle = profile.promptSystemInstruction,
                    isVoicePersonaActive = true
                )
                repository.saveUserSettings(updatedSettings)

                // Apply parameters to speech engine
                voiceEngine.setSpeechParameters(
                    rate = profile.rate,
                    pitch = profile.pitch,
                    languageCode = updatedSettings.voiceLanguage
                )

                // Speak test sample phrase with the newly cloned voice!
                voiceEngine.speak(profile.catchphrase)

                _isAnalyzingVoice.value = false
                onResult(true, "Voix de « ${profile.characterName} » calibrée et activée avec succès !")
            } catch (e: Exception) {
                _isAnalyzingVoice.value = false
                onResult(false, "Erreur lors de l'analyse vocale : ${e.localizedMessage ?: "Échec"}")
            }
        }
    }

    fun toggleVoicePersona(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.getUserSettingsDirect()
            val updated = currentSettings.copy(isVoicePersonaActive = enabled)
            repository.saveUserSettings(updated)
            val (rate, pitch) = if (enabled && updated.voicePersonaName.isNotBlank()) {
                updated.voicePersonaRate to updated.voicePersonaPitch
            } else {
                updated.speechRate to updated.speechPitch
            }
            voiceEngine.setSpeechParameters(rate = rate, pitch = pitch, languageCode = updated.voiceLanguage)
        }
    }

    fun resetVoicePersona() {
        viewModelScope.launch {
            val currentSettings = repository.getUserSettingsDirect()
            val updated = currentSettings.copy(
                voicePersonaName = "",
                voicePersonaDescription = "",
                voicePersonaPitch = 1.0f,
                voicePersonaRate = 1.0f,
                voicePersonaPromptStyle = "",
                isVoicePersonaActive = false
            )
            repository.saveUserSettings(updated)
            voiceEngine.setSpeechParameters(rate = updated.speechRate, pitch = updated.speechPitch, languageCode = updated.voiceLanguage)
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        if (_coreState.value == JarvisCoreState.SPEAKING) {
            _coreState.value = JarvisCoreState.IDLE
        }
    }

    fun stopAll() {
        aiProcessingJob?.cancel()
        _isStreaming.value = false
        _streamingResponse.value = ""
        stopListening()
        stopSpeaking()
        _coreState.value = JarvisCoreState.IDLE
    }

    // Memory operations
    fun saveMemory(key: String, content: String, category: String) {
        viewModelScope.launch {
            repository.saveMemory(key = key, content = content, category = category)
        }
    }

    fun updateMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.updateMemory(memory)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // Note operations
    fun saveNote(title: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.saveNote(title = title, content = content, tags = tags)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Tool Direct Execution
    suspend fun runToolDirect(toolName: String, input: String): String {
        return withContext(Dispatchers.IO) {
            val result = toolEngine.executeTool(toolName, input)
            result.result
        }
    }

    fun togglePinConversation(conversation: ConversationEntity) {
        viewModelScope.launch {
            repository.updateConversation(conversation.copy(isPinned = !conversation.isPinned))
        }
    }

    // Authentication State
    private val _isAuthDialogVisible = MutableStateFlow(false)
    val isAuthDialogVisible: StateFlow<Boolean> = _isAuthDialogVisible.asStateFlow()

    fun showAuthDialog() {
        _isAuthDialogVisible.value = true
    }

    fun hideAuthDialog() {
        _isAuthDialogVisible.value = false
    }

    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = displayName.trim().ifBlank { trimmedEmail.substringBefore("@") }

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            onResult(false, "Veuillez saisir une adresse email valide.")
            return
        }
        if (password.length < 6) {
            onResult(false, "Le mot de passe doit comporter au moins 6 caractères.")
            return
        }

        viewModelScope.launch {
            val existing = repository.getAccountByEmail(trimmedEmail)
            if (existing != null) {
                onResult(false, "Un compte existe déjà avec cette adresse email. Veuillez vous connecter.")
                return@launch
            }

            val newAccount = com.example.data.entity.UserAccountEntity(
                email = trimmedEmail,
                displayName = trimmedName,
                passwordHash = password, // Local secured credential store
                authProvider = "email",
                clearanceLevel = "LEVEL 5 (COMMANDER)"
            )
            repository.createOrUpdateAccount(newAccount)

            val current = repository.getUserSettingsDirect()
            val updated = current.copy(
                userName = trimmedName,
                userEmail = trimmedEmail,
                authProvider = "email",
                isLoggedIn = true,
                securityClearanceLevel = "LEVEL 5 (COMMANDER)"
            )
            repository.saveUserSettings(updated)
            repository.saveMemory(
                key = "Profil Utilisateur",
                content = "Agent $trimmedName ($trimmedEmail) enregistré avec succès.",
                category = "Identity"
            )

            _isAuthDialogVisible.value = false
            onResult(true, "Compte créé avec succès ! Bienvenue, $trimmedName.")
        }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            onResult(false, "Veuillez renseigner votre email.")
            return
        }

        viewModelScope.launch {
            val account = repository.getAccountByEmail(trimmedEmail)
            if (account == null) {
                // Auto-create or suggest register
                val autoName = trimmedEmail.substringBefore("@")
                val newAccount = com.example.data.entity.UserAccountEntity(
                    email = trimmedEmail,
                    displayName = autoName,
                    passwordHash = password,
                    authProvider = "email",
                    clearanceLevel = "LEVEL 5 (COMMANDER)"
                )
                repository.createOrUpdateAccount(newAccount)

                val current = repository.getUserSettingsDirect()
                val updated = current.copy(
                    userName = autoName,
                    userEmail = trimmedEmail,
                    authProvider = "email",
                    isLoggedIn = true,
                    securityClearanceLevel = "LEVEL 5 (COMMANDER)"
                )
                repository.saveUserSettings(updated)
                _isAuthDialogVisible.value = false
                onResult(true, "Compte initialisé et connecté : $autoName.")
                return@launch
            }

            if (account.passwordHash.isNotBlank() && account.passwordHash != password) {
                onResult(false, "Mot de passe incorrect. Veuillez vérifier votre saisie.")
                return@launch
            }

            repository.createOrUpdateAccount(account.copy(lastLoginAt = System.currentTimeMillis()))
            val current = repository.getUserSettingsDirect()
            val updated = current.copy(
                userName = account.displayName.ifBlank { trimmedEmail.substringBefore("@") },
                userEmail = account.email,
                authProvider = "email",
                isLoggedIn = true,
                securityClearanceLevel = account.clearanceLevel
            )
            repository.saveUserSettings(updated)
            _isAuthDialogVisible.value = false
            onResult(true, "Authentification réussie. Re-bienvenue, ${updated.userName}.")
        }
    }

    fun signInWithGoogle(name: String, email: String) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase().ifBlank { "stark.commander@jarvis.ai" }
            val cleanName = name.trim().ifBlank { "Tony Stark" }

            val existing = repository.getAccountByEmail(cleanEmail)
            val account = existing?.copy(
                displayName = cleanName,
                lastLoginAt = System.currentTimeMillis()
            ) ?: com.example.data.entity.UserAccountEntity(
                email = cleanEmail,
                displayName = cleanName,
                authProvider = "google",
                clearanceLevel = "LEVEL 5 (SUPREME COMMANDER)"
            )
            repository.createOrUpdateAccount(account)

            val current = repository.getUserSettingsDirect()
            val updated = current.copy(
                userName = cleanName,
                userEmail = cleanEmail,
                authProvider = "google",
                isLoggedIn = true,
                securityClearanceLevel = "LEVEL 5 (SUPREME COMMANDER)"
            )
            repository.saveUserSettings(updated)
            repository.saveMemory(
                key = "Identité Google",
                content = "Connecté via Google SSO ($cleanEmail) sous le nom de $cleanName",
                category = "Identity"
            )
            _isAuthDialogVisible.value = false
        }
    }

    fun registerOrLoginWithPhone(
        phone: String,
        name: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanPhone = phone.trim()
        if (cleanPhone.length < 6) {
            onResult(false, "Numéro de téléphone incomplet ou invalide.")
            return
        }

        viewModelScope.launch {
            val displayName = name.trim().ifBlank { "Agent $cleanPhone" }
            val existing = repository.getAccountByPhone(cleanPhone)
            val account = existing?.copy(
                displayName = displayName,
                lastLoginAt = System.currentTimeMillis()
            ) ?: com.example.data.entity.UserAccountEntity(
                phone = cleanPhone,
                displayName = displayName,
                authProvider = "phone",
                clearanceLevel = "LEVEL 4 (TACTICAL OPERATOR)"
            )
            repository.createOrUpdateAccount(account)

            val current = repository.getUserSettingsDirect()
            val updated = current.copy(
                userName = displayName,
                userPhone = cleanPhone,
                authProvider = "phone",
                isLoggedIn = true,
                securityClearanceLevel = "LEVEL 4 (TACTICAL OPERATOR)"
            )
            repository.saveUserSettings(updated)
            _isAuthDialogVisible.value = false
            onResult(true, "Connexion par numéro de téléphone validée : $displayName.")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val current = repository.getUserSettingsDirect()
            val updated = current.copy(
                userName = "Sir",
                userEmail = "",
                userPhone = "",
                authProvider = "guest",
                isLoggedIn = false,
                securityClearanceLevel = "LEVEL 1 (GUEST)"
            )
            repository.saveUserSettings(updated)
        }
    }

    // Settings
    fun updateUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
            voiceEngine.setSpeechParameters(
                rate = settings.speechRate,
                pitch = settings.speechPitch,
                languageCode = settings.voiceLanguage
            )
        }
    }

    fun factoryReset() {
        viewModelScope.launch {
            stopAll()
            repository.clearAllConversations()
            repository.clearToolLogs()
            _currentConversation.value = null
            _messages.value = emptyList()
            startNewConversation()
        }
    }

    // Incident & AI Council Operations
    fun triggerSimulatedIncident(onComplete: (com.example.data.entity.IncidentReportEntity) -> Unit) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect()
            val simulatedException = IllegalStateException("Simulation d'exception contrôlée : Test de résilience de la passerelle de streaming")
            val incident = incidentEngine.handleIncident(
                throwable = simulatedException,
                contextInfo = "Test déclenché manuellement depuis les Paramètres",
                settings = settings
            )
            onComplete(incident)
        }
    }

    fun deleteIncident(id: Long) {
        viewModelScope.launch {
            repository.deleteIncident(id)
        }
    }

    fun clearAllIncidents() {
        viewModelScope.launch {
            repository.clearAllIncidents()
        }
    }

    fun reConveneAiCouncil(incident: com.example.data.entity.IncidentReportEntity) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect()
            val updated = incidentEngine.conveneAiResolutionCouncil(incident, settings)
            repository.updateIncident(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
