package com.openmacro.core.engine.action

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.openmacro.core.engine.ExecutionContext
import com.openmacro.core.model.ActionConfig
import com.openmacro.core.model.config.SpeakTextConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

class SpeakTextHandler @Inject constructor() : ActionHandler {
    override val actionTypeId = "speak_text"

    override suspend fun execute(config: ActionConfig, context: ExecutionContext) {
        val parsed = Json.decodeFromString<SpeakTextConfig>(config.configJson)
        if (parsed.text.isBlank()) return

        suspendCancellableCoroutine { cont ->
            var tts: TextToSpeech? = null
            tts = TextToSpeech(context.androidContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech

                    // Set language
                    if (parsed.language.isNotBlank()) {
                        val locale = Locale.forLanguageTag(parsed.language)
                        engine.language = locale
                    }

                    engine.setPitch(parsed.pitch.coerceIn(0.1f, 4.0f))
                    engine.setSpeechRate(parsed.speed.coerceIn(0.1f, 4.0f))

                    val utteranceId = UUID.randomUUID().toString()
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}
                        override fun onDone(id: String?) {
                            engine.shutdown()
                            if (cont.isActive) cont.resume(Unit)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?) {
                            engine.shutdown()
                            if (cont.isActive) cont.resume(Unit)
                        }
                    })

                    engine.speak(parsed.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    Log.e(TAG, "TTS initialization failed with status: $status")
                    if (cont.isActive) cont.resume(Unit)
                }
            }

            cont.invokeOnCancellation {
                tts?.stop()
                tts?.shutdown()
            }
        }
    }

    companion object {
        private const val TAG = "SpeakTextHandler"
    }
}
