package com.example.engine.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _voiceStatus = MutableStateFlow("VOICE ENGINE INITIALIZING")
    val voiceStatus: StateFlow<String> = _voiceStatus.asStateFlow()

    var onSpeechComplete: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.let {
                // Try French first as default, fallback to English / Default
                val result = it.setLanguage(Locale.FRENCH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    it.setLanguage(Locale.ENGLISH)
                }
                it.setPitch(1.0f)
                it.setSpeechRate(1.05f)

                it.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioAmplitude.value = 0f
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _audioAmplitude.value = 0f
                    }
                })
            }
            _voiceStatus.value = "VOICE ENGINE ONLINE"
        } else {
            _voiceStatus.value = "TTS UNAVAILABLE"
        }
    }

    fun setSpeechParameters(rate: Float, pitch: Float, languageCode: String) {
        tts?.let {
            it.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            it.setPitch(pitch.coerceIn(0.5f, 2.0f))
            val locale = when (languageCode.lowercase()) {
                "fr" -> Locale.FRENCH
                "en" -> Locale.ENGLISH
                else -> Locale.getDefault()
            }
            it.setLanguage(locale)
        }
    }

    fun speak(text: String, utteranceId: String = "JARVIS_RESPONSE") {
        if (!isTtsInitialized || tts == null) return
        stopListening()
        stopSpeaking()

        // Clean markdown for audio
        val cleanText = cleanMarkdownForSpeech(text)
        if (cleanText.isBlank()) return

        _isSpeaking.value = true
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
            _audioAmplitude.value = 0f
        }
    }

    fun startListening() {
        stopSpeaking()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError?.invoke("Speech Recognition is not available on this device.")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _recognizedText.value = ""
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS dB to a 0.0 - 1.0 amplitude
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _audioAmplitude.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioAmplitude.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioAmplitude.value = 0f
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        else -> "Speech recognition error code: $error"
                    }
                    onSpeechError?.invoke(msg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioAmplitude.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        _recognizedText.value = spokenText
                        onSpeechComplete?.invoke(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _recognizedText.value = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onSpeechError?.invoke("Could not start voice recognition: ${e.message}")
        }
    }

    fun stopListening() {
        if (_isListening.value) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
            _audioAmplitude.value = 0f
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun cleanMarkdownForSpeech(text: String): String {
        return text
            .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?\\n```"), " Code block omitted for audio summary. ")
            .replace(Regex("`[^`]+`"), "")
            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
            .replace(Regex("[*#_~>]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
