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

                    val errMsg = MessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = "Une interruption de protocole est survenue : ${e.localizedMessage ?: "Erreur interne"}",
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

    fun speakText(text: String) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect()
            voiceEngine.setSpeechParameters(
                rate = settings.speechRate,
                pitch = settings.speechPitch,
                languageCode = settings.voiceLanguage
            )
            voiceEngine.speak(text)
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

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
