package com.thackman.ai.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechService(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRENCH
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.05f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "THACKMAN_AUDIO_ID")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
