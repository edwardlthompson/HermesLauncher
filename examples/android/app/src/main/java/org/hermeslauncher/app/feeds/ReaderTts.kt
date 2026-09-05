package org.hermeslauncher.app.feeds

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class ReaderTts(context: Context) {
    private var ready = false
    private var engine: TextToSpeech? = null

    init {
        engine = runCatching {
            TextToSpeech(context.applicationContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    runCatching { engine?.language = Locale.getDefault() }
                }
            }
        }.getOrNull()
    }

    fun speak(text: String) {
        val tts = engine ?: return
        val body = text.trim()
        if (!ready || body.isBlank()) {
            return
        }
        runCatching { tts.speak(body, TextToSpeech.QUEUE_FLUSH, null, "hermes-reader") }
    }

    fun stop() {
        runCatching { engine?.stop() }
    }

    fun shutdown() {
        runCatching {
            engine?.stop()
            engine?.shutdown()
        }
    }
}
