package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms = _audioRms.asStateFlow()

    private val _userTranscript = MutableStateFlow("")
    val userTranscript = _userTranscript.asStateFlow()

    private val _aiTranscript = MutableStateFlow("Hi! I'm your Bartr Live Voice AI. What can I help you with today?")
    val aiTranscript = _aiTranscript.asStateFlow()

    private val _pendingPermission = MutableStateFlow<PendingPermission?>(null)
    val pendingPermission = _pendingPermission.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _groundings = MutableStateFlow<List<GroundingSource>>(emptyList())
    val groundings = _groundings.asStateFlow()

    private val _currentModel = MutableStateFlow("gemini-3.8-flash")
    val currentModel = _currentModel.asStateFlow()

    private val _liveConnectionInfo = MutableStateFlow("Gemini 3.8 Live & Maps Grounding Ready")
    val liveConnectionInfo = _liveConnectionInfo.asStateFlow()

    private var liveWebSocketClient: GeminiLiveWebSocketClient? = null

    private val history = mutableListOf<AiChatMessage>()

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
                val result = tts.setLanguage(Locale.ENGLISH)
                tts.setPitch(1.05f)
                tts.setSpeechRate(1.0f)
                isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            }
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            textToSpeech?.stop()
        }
    }

    fun startListening() {
        textToSpeech?.stop()
        _userTranscript.value = ""
        _audioRms.value = 0f

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
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
            return
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gemini_voice_${System.currentTimeMillis()}")
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
        _voiceState.value = VoiceState.IDLE
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val bestResult = matches?.firstOrNull()
        if (!bestResult.isNullOrBlank()) {
            _userTranscript.value = bestResult
            processUserUtterance(bestResult)
        } else {
            _voiceState.value = VoiceState.IDLE
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
