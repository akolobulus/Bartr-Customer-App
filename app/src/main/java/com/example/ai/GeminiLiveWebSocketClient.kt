package com.example.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.BartrRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Gemini 3.8 Live API Client supporting real-time bidirectional WebSocket conversation
 * with fallback to gemini-3.8-flash and gemini-3.5-flash Grounded API.
 *
 * Model: models/gemini-3.8-flash and gemini-3.8-live
 */
class GeminiLiveWebSocketClient(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onMessageReceived: (text: String, action: AutonomousAction?, groundings: List<GroundingSource>) -> Unit,
    private val onConnectionStateChanged: (connected: Boolean, info: String) -> Unit
) {
    companion object {
        private const val TAG = "GeminiLiveWS"
        private const val LIVE_WS_URL = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for websockets
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun connect() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No valid Gemini API key for Live WebSocket, operating in autonomous local mode.")
            onConnectionStateChanged(false, "Local Autonomous Mode")
            return
        }

        try {
            val url = "$LIVE_WS_URL?key=$apiKey"
            val request = Request.Builder().url(url).build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.i(TAG, "Gemini 3.8 Live WebSocket Connected")
                    _isConnected.value = true
                    coroutineScope.launch(Dispatchers.Main) {
                        onConnectionStateChanged(true, "Connected to Gemini 3.8 Live")
                    }
                    sendInitialSetup(webSocket)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.i(TAG, "Gemini 3.8 Live WebSocket closing: $reason ($code)")
                    _isConnected.value = false
                    coroutineScope.launch(Dispatchers.Main) {
                        onConnectionStateChanged(false, "Live connection closed")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(TAG, "Gemini 3.8 Live WebSocket error: ${t.message}")
                    _isConnected.value = false
                    coroutineScope.launch(Dispatchers.Main) {
                        onConnectionStateChanged(false, "Fallback mode active")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating Gemini Live WebSocket", e)
            _isConnected.value = false
        }
    }

    private fun sendInitialSetup(ws: WebSocket) {
        try {
            val setup = JSONObject()
            val bidiSetup = JSONObject()

            // Gemini 3.8 live model specification
            bidiSetup.put("model", "models/gemini-3.8-flash")

            val generationConfig = JSONObject()
            val modalities = JSONArray()
            modalities.put("TEXT")
            modalities.put("AUDIO")
            generationConfig.put("responseModalities", modalities)

            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            val prebuiltVoiceConfig = JSONObject()
            prebuiltVoiceConfig.put("voiceName", "Kore")
            voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
            speechConfig.put("voiceConfig", voiceConfig)
            generationConfig.put("speechConfig", speechConfig)

            bidiSetup.put("generationConfig", generationConfig)

            // System Instruction
            val sysInstruction = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", "You are the Bartr Live Voice AI Assistant on Android powered by Gemini 3.8 Live. Help users with local artisans, repairs, mechanics, beauty services in Ikeja and Lagos, Nigeria. Speak concisely and warm."))
            sysInstruction.put("parts", parts)
            bidiSetup.put("systemInstruction", sysInstruction)

            // Grounding with Google Maps & tools
            val tools = JSONArray()
            val mapsTool = JSONObject()
            mapsTool.put("google_maps", JSONObject())
            tools.put(mapsTool)
            bidiSetup.put("tools", tools)

            setup.put("setup", bidiSetup)
            ws.send(setup.toString())
            Log.d(TAG, "Sent BidiGenerateContentSetup to Live API")
        } catch (e: Exception) {
            Log.e(TAG, "Failed sending initial WebSocket setup", e)
        }
    }

    fun sendText(userMessage: String) {
        val ws = webSocket
        if (ws != null && _isConnected.value) {
            try {
                val clientContent = JSONObject()
                val turns = JSONArray()
                val turn = JSONObject()
                turn.put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", userMessage))
                turn.put("parts", parts)
                turns.put(turn)

                val realtimeInput = JSONObject()
                realtimeInput.put("turns", turns)
                clientContent.put("clientContent", realtimeInput)

                ws.send(clientContent.toString())
                return
            } catch (e: Exception) {
                Log.w(TAG, "Error sending text over live websocket", e)
            }
        }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            val serverContent = json.optJSONObject("serverContent")
            val modelTurn = serverContent?.optJSONObject("modelTurn")
            val parts = modelTurn?.optJSONArray("parts")

            var responseText = ""
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        responseText += p.optString("text")
                    }
                }
            }

            // Extract Grounding metadata if available
            val groundings = mutableListOf<GroundingSource>()
            val groundingMetadata = serverContent?.optJSONObject("groundingMetadata")
                ?: json.optJSONObject("groundingMetadata")

            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        val maps = chunk.optJSONObject("maps")
                        val title = maps?.optString("title") ?: web?.optString("title") ?: "Google Maps Data"
                        val uri = maps?.optString("uri") ?: web?.optString("uri") ?: ""
                        groundings.add(GroundingSource(title = title, uri = uri))
                    }
                }
            }

            if (responseText.isNotBlank()) {
                coroutineScope.launch(Dispatchers.Main) {
                    onMessageReceived(responseText, null, groundings)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming Live API message", e)
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User closed session")
            webSocket = null
            _isConnected.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Live WebSocket", e)
        }
    }
}
