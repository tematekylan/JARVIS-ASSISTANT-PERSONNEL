package com.thackman.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thackman.ai.automation.ActionExecutor
import com.thackman.ai.model.AIMessage
import com.thackman.ai.model.AssistantState
import com.thackman.ai.model.MessageSender
import com.thackman.ai.repository.AIRepository
import com.thackman.ai.service.TextToSpeechService
import com.thackman.ai.service.VoiceRecognitionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val actionExecutor = ActionExecutor(application)
    private val voiceService = VoiceRecognitionService(application)
    private val ttsService = TextToSpeechService(application)
    private val aiRepository = AIRepository()

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState = _assistantState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0.2f)
    val audioAmplitude = _audioAmplitude.asStateFlow()

    private val _conversation = MutableStateFlow<List<AIMessage>>(
        listOf(
            AIMessage(
                sender = MessageSender.AI_ASSISTANT,
                text = "Bonjour Commandant. Tous les systèmes T-HACKMAN AI sont opérationnels."
            )
        )
    )
    val conversation = _conversation.asStateFlow()

    fun toggleVoiceRecognition() {
        if (_assistantState.value == AssistantState.LISTENING) {
            voiceService.stopListening()
            _assistantState.value = AssistantState.IDLE
        } else {
            _assistantState.value = AssistantState.LISTENING
            voiceService.startListening { query ->
                executeCommand(query)
            }
        }
    }

    fun executeCommand(prompt: String) {
        val userMsg = AIMessage(sender = MessageSender.USER, text = prompt)
        _conversation.value = _conversation.value + userMsg

        // 1. Check local automation
        val autoResult = actionExecutor.execute(prompt)
        if (autoResult.executed) {
            val reply = autoResult.responseMessage ?: "Commande exécutée avec succès."
            val aiMsg = AIMessage(sender = MessageSender.AI_ASSISTANT, text = reply)
            _conversation.value = _conversation.value + aiMsg
            _assistantState.value = AssistantState.SPEAKING
            ttsService.speak(reply)
            viewModelScope.launch {
                delay(2500)
                _assistantState.value = AssistantState.IDLE
            }
            return
        }

        // 2. Query AI Model
        _assistantState.value = AssistantState.THINKING
        viewModelScope.launch {
            val answer = aiRepository.queryAssistant(prompt)
            val aiMsg = AIMessage(sender = MessageSender.AI_ASSISTANT, text = answer)
            _conversation.value = _conversation.value + aiMsg
            _assistantState.value = AssistantState.SPEAKING
            ttsService.speak(answer)
            delay(3000)
            _assistantState.value = AssistantState.IDLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
        voiceService.stopListening()
    }
}
