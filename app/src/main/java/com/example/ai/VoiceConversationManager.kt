package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    AWAITING_PERMISSION
}

class VoiceConversationManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onExecuteAction: (AutonomousAction) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val TAG = "VoiceManager"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isSessionActive = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms = _audioRms.asStateFlow()

    private val _userTranscript = MutableStateFlow("")
    val userTranscript = _userTranscript.asStateFlow()

    private val _aiTranscript = MutableStateFlow("Hi! I'm your Bartr voice assistant. How far? What can I help you sort out today?")
    val aiTranscript = _aiTranscript.asStateFlow()

    private val _pendingPermission = MutableStateFlow<PendingPermission?>(null)
    val pendingPermission = _pendingPermission.asStateFlow()

    // Speaker mute
    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    // User explicitly paused mic
    private val _isMicPaused = MutableStateFlow(false)
    val isMicPaused = _isMicPaused.asStateFlow()

    private val _groundings = MutableStateFlow<List<GroundingSource>>(emptyList())
    val groundings = _groundings.asStateFlow()

    private val _currentModel = MutableStateFlow("gemini-3.8-flash")
    val currentModel = _currentModel.asStateFlow()

    private val _liveConnectionInfo = MutableStateFlow("Gemini 3.8 Live & Maps Grounding Ready")
    val liveConnectionInfo = _liveConnectionInfo.asStateFlow()

    private var liveWebSocketClient: GeminiLiveWebSocketClient? = null

    private val history = mutableListOf<AiChatMessage>()

    private val restartListeningRunnable = Runnable {
        if (isSessionActive && !_isMicPaused.value && _voiceState.value != VoiceState.SPEAKING && _voiceState.value != VoiceState.THINKING) {
            try {
                speechRecognizer?.cancel()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                }
                speechRecognizer?.startListening(intent)
                _voiceState.value = VoiceState.LISTENING
            } catch (e: Exception) {
                Log.w(TAG, "Restart listening attempt: ${e.message}")
                recreateSpeechRecognizer()
            }
        }
    }

    private fun scheduleListeningRestart(delayMs: Long) {
        mainHandler.removeCallbacks(restartListeningRunnable)
        mainHandler.postDelayed(restartListeningRunnable, delayMs)
    }

    private fun recreateSpeechRecognizer() {
        try {
            speechRecognizer?.destroy()
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceConversationManager)
                }
                if (isSessionActive && !_isMicPaused.value) {
                    scheduleListeningRestart(350L)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recreate speech recognizer error", e)
        }
    }

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceConversationManager)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SpeechRecognizer initialization failed", e)
        }

        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "TextToSpeech initialization failed", e)
        }

        // Initialize Gemini 3.8 Live WebSocket client
        try {
            liveWebSocketClient = GeminiLiveWebSocketClient(
                context = context,
                coroutineScope = coroutineScope,
                onMessageReceived = { text, action, newGroundings ->
                    _aiTranscript.value = text
                    if (newGroundings.isNotEmpty()) {
                        _groundings.value = newGroundings
                    }
                    _voiceState.value = VoiceState.SPEAKING
                    speakText(text)
                    if (action != null) {
                        launchAction(action)
                    }
                },
                onConnectionStateChanged = { connected, info ->
                    _liveConnectionInfo.value = info
                }
            ).apply {
                connect()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Live WebSocket initialization notice: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                configureSoftNigerianVoice(tts)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            // After AI finishes speaking, immediately resume listening for continuous dialogue!
                            if (isSessionActive && !_isMicPaused.value) {
                                scheduleListeningRestart(300L)
                            } else {
                                _voiceState.value = VoiceState.IDLE
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            if (isSessionActive && !_isMicPaused.value) {
                                scheduleListeningRestart(300L)
                            } else {
                                _voiceState.value = VoiceState.IDLE
                            }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        mainHandler.post {
                            if (isSessionActive && !_isMicPaused.value) {
                                scheduleListeningRestart(300L)
                            } else {
                                _voiceState.value = VoiceState.IDLE
                            }
                        }
                    }
                })
            }
        }
    }

    /**
     * Specifically configures a soft, warm Nigerian female voice with gentle pitch and cadence.
     */
    private fun configureSoftNigerianVoice(tts: TextToSpeech) {
        try {
            // Set locale to English (Nigeria)
            val nigerianLocale = Locale("en", "NG")
            val langResult = tts.setLanguage(nigerianLocale)

            // Inspect available voices to select the best Nigerian / soft female voice
            val voices = tts.voices
            if (!voices.isNullOrEmpty()) {
                // 1. High-priority: Nigerian English female voice
                val nigerianFemale = voices.firstOrNull { voice ->
                    val loc = voice.locale
                    val isNigerian = loc.country.equals("NG", ignoreCase = true) ||
                            loc.toLanguageTag().contains("NG", ignoreCase = true)
                    val isFemale = voice.name.contains("female", ignoreCase = true) ||
                            voice.name.contains("f00", ignoreCase = true) ||
                            voice.name.contains("#female", ignoreCase = true) ||
                            (!voice.name.contains("male", ignoreCase = true) && voice.name.contains("ng", ignoreCase = true))
                    isNigerian && isFemale
                }

                // 2. Any Nigerian voice
                val nigerianAny = voices.firstOrNull { voice ->
                    val loc = voice.locale
                    loc.country.equals("NG", ignoreCase = true) || loc.toLanguageTag().contains("NG", ignoreCase = true)
                }

                // 3. African female voice (e.g. ZA/KE/GH)
                val africanFemale = voices.firstOrNull { voice ->
                    val loc = voice.locale
                    (loc.country.equals("ZA", ignoreCase = true) || loc.country.equals("KE", ignoreCase = true) || loc.country.equals("GH", ignoreCase = true)) &&
                            (voice.name.contains("female", ignoreCase = true) || voice.name.contains("#female", ignoreCase = true))
                }

                // 4. Soft female English voice
                val softFemale = voices.firstOrNull { voice ->
                    voice.locale.language.equals("en", ignoreCase = true) &&
                            (voice.name.contains("female", ignoreCase = true) ||
                             voice.name.contains("#female", ignoreCase = true) ||
                             voice.name.contains("en-gb", ignoreCase = true))
                }

                val selectedVoice = nigerianFemale ?: nigerianAny ?: africanFemale ?: softFemale
                if (selectedVoice != null) {
                    tts.voice = selectedVoice
                    Log.i(TAG, "Configured soft Nigerian voice: ${selectedVoice.name} (${selectedVoice.locale})")
                }
            }

            // Tune pitch to 1.15f for a pleasant, soft, friendly feminine register
            tts.setPitch(1.15f)
            // Tune speech rate to 1.16f for a brisk, faster, lively conversational pace
            tts.setSpeechRate(1.16f)
            isTtsReady = (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED)
            if (!isTtsReady) {
                // If en_NG data is missing in local TTS engine, fallback language to standard English while keeping pitch and rate
                tts.setLanguage(Locale.ENGLISH)
                isTtsReady = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting soft Nigerian voice", e)
            try {
                tts.setLanguage(Locale.ENGLISH)
                tts.setPitch(1.15f)
                tts.setSpeechRate(1.16f)
                isTtsReady = true
            } catch (ignored: Exception) {}
        }
    }

    fun startSession() {
        isSessionActive = true
        _isMicPaused.value = false
        mainHandler.post {
            startListening()
        }
    }

    fun endSession() {
        isSessionActive = false
        mainHandler.removeCallbacksAndMessages(null)
        stopListening()
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS: ${e.message}")
        }
        _voiceState.value = VoiceState.IDLE
    }

    fun toggleMicPause() {
        if (_isMicPaused.value) {
            _isMicPaused.value = false
            startListening()
        } else {
            _isMicPaused.value = true
            mainHandler.removeCallbacks(restartListeningRunnable)
            stopListening()
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            textToSpeech?.stop()
            if (isSessionActive && !_isMicPaused.value) {
                scheduleListeningRestart(300L)
            }
        }
    }

    fun startListening() {
        if (!isSessionActive) {
            isSessionActive = true
        }
        _isMicPaused.value = false
        mainHandler.removeCallbacks(restartListeningRunnable)

        // Stop TTS if speaking so user can interrupt at any point
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            // ignore
        }

        _userTranscript.value = ""
        _audioRms.value = 0f

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
        }

        try {
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            if (isSessionActive && !_isMicPaused.value) {
                scheduleListeningRestart(500L)
            } else {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    fun stopListening() {
        mainHandler.removeCallbacks(restartListeningRunnable)
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
        _voiceState.value = VoiceState.IDLE
    }

    fun submitTextUtterance(text: String) {
        if (text.isBlank()) return
        _userTranscript.value = text
        processUserUtterance(text)
    }

    private fun processUserUtterance(userInput: String) {
        val trimmed = userInput.trim()
        val lower = trimmed.lowercase()

        // Check if we are currently awaiting permission for an action
        val currentPending = _pendingPermission.value
        if (currentPending != null) {
            if (isAffirmative(lower)) {
                confirmPendingAction()
                return
            } else if (isNegative(lower)) {
                dismissPendingAction()
                return
            }
        }

        _voiceState.value = VoiceState.THINKING
        history.add(AiChatMessage(role = "user", text = trimmed))

        // Also stream to live WebSocket if connected
        liveWebSocketClient?.sendText(trimmed)

        coroutineScope.launch {
            val result = GeminiLiveService.sendMessage(history, trimmed)
            result.onSuccess { response ->
                _aiTranscript.value = response.text
                _currentModel.value = response.modelUsed
                _groundings.value = response.groundings
                history.add(AiChatMessage(role = "model", text = response.text))

                val action = response.action
                if (action != null) {
                    if (action.requiresPermission) {
                        _voiceState.value = VoiceState.AWAITING_PERMISSION
                        val permission = createPendingPermission(action)
                        _pendingPermission.value = permission
                        speakText("${response.text}. Do you approve?")
                    } else {
                        // Autonomous execution permitted
                        _voiceState.value = VoiceState.SPEAKING
                        speakText(response.text)
                        launchAction(action)
                    }
                } else {
                    _voiceState.value = VoiceState.SPEAKING
                    speakText(response.text)
                }
            }.onFailure { err ->
                val fallbackMsg = "I had trouble with the network. Please try again."
                _aiTranscript.value = fallbackMsg
                _voiceState.value = VoiceState.SPEAKING
                speakText(fallbackMsg)
            }
        }
    }

    private fun launchAction(action: AutonomousAction) {
        coroutineScope.launch(Dispatchers.Main) {
            onExecuteAction(action)
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingPermission.value ?: return
        _pendingPermission.value = null
        val confirmationMsg = "Action confirmed! Executing now."
        _aiTranscript.value = confirmationMsg
        speakText(confirmationMsg)
        _voiceState.value = VoiceState.IDLE
        coroutineScope.launch(Dispatchers.Main) {
            onExecuteAction(pending.action)
        }
    }

    fun dismissPendingAction() {
        _pendingPermission.value = null
        val cancelMsg = "Cancelled. I won't proceed with that request."
        _aiTranscript.value = cancelMsg
        speakText(cancelMsg)
        _voiceState.value = VoiceState.IDLE
    }

    private fun isAffirmative(text: String): Boolean {
        return text in listOf("yes", "yeah", "yep", "confirm", "proceed", "go ahead", "do it", "sure", "ok", "okay", "approve", "authorize") ||
                text.startsWith("yes") || text.contains("go ahead") || text.contains("do it")
    }

    private fun isNegative(text: String): Boolean {
        return text in listOf("no", "nope", "cancel", "stop", "don't", "dont", "dismiss", "reject") ||
                text.startsWith("no") || text.contains("don't")
    }

    private fun createPendingPermission(action: AutonomousAction): PendingPermission {
        return when (action) {
            is AutonomousAction.BookVendor -> PendingPermission(
                id = UUID.randomUUID().toString(),
                action = action,
                title = "Request ${action.vendorName}",
                explanation = "AI wants to send a verified service booking for ${action.service}.",
                details = mapOf(
                    "Vendor" to action.vendorName,
                    "Service" to action.service,
                    "Estimated Price" to action.price,
                    "Payment Terms" to "Cash on Completion"
                )
            )
            else -> PendingPermission(
                id = UUID.randomUUID().toString(),
                action = action,
                title = "Action Authorization",
                explanation = "AI is requesting permission to execute: ${action.summary}",
                details = mapOf("Action" to action.summary)
            )
        }
    }

    private fun speakText(text: String) {
        if (_isMuted.value || !isTtsReady) {
            _voiceState.value = VoiceState.IDLE
            if (isSessionActive && !_isMicPaused.value) {
                scheduleListeningRestart(800L)
            }
            return
        }
        _voiceState.value = VoiceState.SPEAKING
        val utteranceId = "bartr_voice_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        textToSpeech?.let { tts ->
            tts.setPitch(1.15f)
            tts.setSpeechRate(1.16f)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    // SpeechRecognizer Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onBeginningOfSpeech() {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalizes to 0..1 range
        val normalized = (rmsdB / 10f).coerceIn(0f, 1f)
        _audioRms.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = VoiceState.THINKING
    }

    override fun onError(error: Int) {
        Log.w(TAG, "Speech recognition error code: $error")
        // In continuous listening mode, do not turn off the mic! Keep listening active seamlessly.
        if (isSessionActive && !_isMicPaused.value && _voiceState.value != VoiceState.SPEAKING && _voiceState.value != VoiceState.THINKING) {
            val delay = when (error) {
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                SpeechRecognizer.ERROR_NO_MATCH -> 250L
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 450L
                SpeechRecognizer.ERROR_CLIENT -> 500L
                else -> 600L
            }
            scheduleListeningRestart(delay)
        } else {
            _voiceState.value = VoiceState.IDLE
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val bestResult = matches?.firstOrNull()
        if (!bestResult.isNullOrBlank()) {
            _userTranscript.value = bestResult
            processUserUtterance(bestResult)
        } else {
            // No match found, seamlessly keep listening so user doesn't have to tap or unmute
            if (isSessionActive && !_isMicPaused.value && _voiceState.value != VoiceState.SPEAKING && _voiceState.value != VoiceState.THINKING) {
                scheduleListeningRestart(250L)
            } else {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()
        if (!partial.isNullOrBlank()) {
            _userTranscript.value = partial
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        endSession()
        try {
            liveWebSocketClient?.disconnect()
            liveWebSocketClient = null
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up VoiceManager", e)
        }
    }
}
