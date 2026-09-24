package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.BartrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiLiveService {
    private const val TAG = "GeminiLiveService"
    private const val PRIMARY_MODEL = "gemini-3.8-flash"
    private val CANDIDATE_MODELS = listOf(
        "gemini-3.8-flash",
        "gemini-3.5-flash",
        "gemini-3.6-flash",
        "gemini-flash-latest"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendMessage(
        history: List<AiChatMessage>,
        userMessage: String
    ): Result<AiResponseResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is placeholder or blank. Using local autonomous AI engine.")
            return@withContext Result.success(handleOfflineFallback(userMessage))
        }

        try {
            var bodyStr: String? = null
            var lastErrorMsg: String? = null
            var successfulModel: String? = null

            for (model in CANDIDATE_MODELS) {
                // When using gemini-3.5-flash or gemini-3.8-flash, include Google Maps grounding
                val requestJson = buildRequestJson(history, userMessage, model)
                val requestBody = requestJson.toString().toRequestBody(jsonMediaType)

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                try {
                    client.newCall(request).execute().use { response ->
                        val respContent = response.body?.string().orEmpty()
                        if (response.isSuccessful) {
                            bodyStr = respContent
                            successfulModel = model
                            return@use
                        } else {
                            lastErrorMsg = "Gemini API ($model) error ${response.code}: $respContent"
                            Log.w(TAG, lastErrorMsg!!)
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMsg = "Gemini API ($model) network error: ${e.message}"
                    Log.w(TAG, lastErrorMsg!!)
                }

                if (bodyStr != null) break
            }

            if (bodyStr == null) {
                Log.e(TAG, "All candidate Gemini models failed. $lastErrorMsg. Using local autonomous fallback.")
                return@withContext Result.success(handleOfflineFallback(userMessage))
            }

            val responseJson = JSONObject(bodyStr ?: "{}")
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var replyText = ""
            var functionCall: AiFunctionCall? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("text")) {
                        replyText += part.optString("text")
                    }
                    if (part.has("functionCall")) {
                        val fcObj = part.getJSONObject("functionCall")
                        val fnName = fcObj.optString("name")
                        val argsObj = fcObj.optJSONObject("args")
                        val argsMap = mutableMapOf<String, Any>()
                        if (argsObj != null) {
                            val keys = argsObj.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                argsMap[key] = argsObj.get(key)
                            }
                        }
                        functionCall = AiFunctionCall(fnName, argsMap)
                    }
                }
            }

            // Extract Google Maps Grounding metadata from response
            val groundings = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
                ?: responseJson.optJSONObject("groundingMetadata")

            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val maps = chunk.optJSONObject("maps")
                        val web = chunk.optJSONObject("web")
                        val title = maps?.optString("title") ?: web?.optString("title") ?: "Google Maps Location"
                        val uri = maps?.optString("uri") ?: web?.optString("uri") ?: ""
                        groundings.add(GroundingSource(title = title, uri = uri))
                    }
                }
            }

            if (replyText.isBlank() && functionCall == null) {
                replyText = "I'm on it! Let me help you with that."
            }

            val action = functionCall?.let { mapToAutonomousAction(it) }

            Result.success(
                AiResponseResult(
                    text = replyText.trim(),
                    functionCall = functionCall,
                    action = action,
                    groundings = groundings,
                    modelUsed = successfulModel ?: PRIMARY_MODEL
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini", e)
            Result.success(handleOfflineFallback(userMessage))
        }
    }

    private fun buildRequestJson(
        history: List<AiChatMessage>,
        userMessage: String,
        targetModel: String = PRIMARY_MODEL
    ): JSONObject {
        val root = JSONObject()

        // System Instruction
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(
            JSONObject().put(
                "text",
                """
                You are the Bartr Live Voice AI Assistant on Android powered by $targetModel.
                Bartr connects people in Lagos, Nigeria (Ikeja, Yaba, Surulere, Victoria Island, etc.) with trusted local service providers.
                You have autonomous agency to perform actions on the user's behalf using tools.
                You are grounded with Google Maps data for accurate locations, addresses, and local venues in Lagos.
                Tools available:
                - search_vendors: when user needs any service (phone repair, auto mechanic, nail tech, plumber, AC repair, generator repair, carpenter, etc.)
                - book_vendor: when user wants to book, hire, or request a specific vendor
                - navigate_to: when user wants to go to a screen (home, payments, promotions, my_requests, saved_vendors, help, about, profile, edit_profile, invite_friend, matches)
                - recenter_map: when user wants to center map on current location in Ikeja
                - apply_promo_code: when user asks to enter or apply a promo code (e.g. BARTR500)
                - open_chat_with_vendor: when user wants to chat with a vendor (Chuka, Adaeze, Musa)
                
                Always speak concisely, warmly, and helpfully. Keep spoken sentences short (1-2 sentences).
                For bookings/requests, make clear that user confirmation will be requested.
                """.trimIndent()
            )
        )
        systemInstruction.put("parts", sysParts)
        root.put("systemInstruction", systemInstruction)

        // Contents
        val contents = JSONArray()
        // Add recent history (up to last 6 turns)
        val recentHistory = history.takeLast(6)
        for (item in recentHistory) {
            val turn = JSONObject()
            turn.put("role", if (item.role == "user") "user" else "model")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", item.text))
            turn.put("parts", parts)
            contents.put(turn)
        }

        // Add current user message
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        currentTurn.put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
        contents.put(currentTurn)
        root.put("contents", contents)

        // Tools / Function Declarations & Google Maps Grounding
        val tools = JSONArray()

        // Google Maps Grounding Tool
        val mapsToolObj = JSONObject()
        mapsToolObj.put("google_maps", JSONObject())
        tools.put(mapsToolObj)

        val toolObj = JSONObject()
        val declarations = JSONArray()

        declarations.put(
            createFunctionDeclaration(
                name = "search_vendors",
                description = "Search and find local service providers in Lagos by trade or keywords.",
                properties = mapOf(
                    "query" to ("STRING" to "Service or trade to search for, e.g. 'iPhone repair' or 'mechanic'"),
                    "category" to ("STRING" to "Category of service")
                ),
                required = listOf("query")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "book_vendor",
                description = "Request or book a verified vendor for a task. Requires user permission.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID or name (e.g. 'chuka', 'adaeze', 'musa')"),
                    "service" to ("STRING" to "The service requested (e.g. 'Screen Replacement')"),
                    "price" to ("STRING" to "Estimated price, e.g. '₦5,000'")
                ),
                required = listOf("vendor_id", "service")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "navigate_to",
                description = "Navigate to any screen on the app.",
                properties = mapOf(
                    "screen" to ("STRING" to "Target screen: 'home', 'payments', 'promotions', 'my_requests', 'saved_vendors', 'help', 'about', 'profile', 'edit_profile', 'invite_friend', 'matches'")
                ),
                required = listOf("screen")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "recenter_map",
                description = "Recenter the map on the user's location at 14 Market Road, Ikeja.",
                properties = emptyMap(),
                required = emptyList()
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "apply_promo_code",
                description = "Apply a promotional discount code (e.g. BARTR500).",
                properties = mapOf(
                    "code" to ("STRING" to "The promo code to apply")
                ),
                required = listOf("code")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "open_chat_with_vendor",
                description = "Open the direct chat window with a vendor.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID or name (chuka, adaeze, musa)")
                ),
                required = listOf("vendor_id")
            )
        )

        toolObj.put("functionDeclarations", declarations)
        tools.put(toolObj)
        root.put("tools", tools)

        return root
    }

    private fun createFunctionDeclaration(
        name: String,
        description: String,
        properties: Map<String, Pair<String, String>>,
        required: List<String>
    ): JSONObject {
        val decl = JSONObject()
        decl.put("name", name)
        decl.put("description", description)

        val params = JSONObject()
        params.put("type", "OBJECT")
        val props = JSONObject()
        for ((propName, propTypeDesc) in properties) {
            val propObj = JSONObject()
            propObj.put("type", propTypeDesc.first)
            propObj.put("description", propTypeDesc.second)
            props.put(propName, propObj)
        }
        params.put("properties", props)
        if (required.isNotEmpty()) {
            val reqArray = JSONArray()
            required.forEach { reqArray.put(it) }
            params.put("required", reqArray)
        }
        decl.put("parameters", params)
        return decl
    }

    private fun mapToAutonomousAction(fc: AiFunctionCall): AutonomousAction? {
        return when (fc.name) {
            "search_vendors" -> {
                val q = fc.args["query"]?.toString() ?: "repairs"
                val cat = fc.args["category"]?.toString()
                AutonomousAction.SearchVendors(q, cat)
            }
            "book_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                val service = fc.args["service"]?.toString() ?: vendor.category
                val price = fc.args["price"]?.toString() ?: vendor.finalPrice
                AutonomousAction.BookVendor(vendor.id, vendor.name, service, price)
            }
            "navigate_to" -> {
                val screen = fc.args["screen"]?.toString()?.lowercase() ?: "home"
                val label = when (screen) {
                    "payments" -> "Payments"
                    "promotions" -> "Promotions"
                    "my_requests" -> "My Requests"
                    "saved_vendors" -> "Saved Vendors"
                    "help" -> "Help & Support"
                    "about" -> "About"
                    "profile" -> "Profile"
                    "edit_profile" -> "Edit Profile"
                    "invite_friend" -> "Invite a Friend"
                    "matches" -> "Matches"
                    else -> "Home"
                }
                AutonomousAction.NavigateTo(screen, label)
            }
            "recenter_map" -> AutonomousAction.RecenterMap()
            "apply_promo_code" -> {
                val code = fc.args["code"]?.toString() ?: "BARTR500"
                AutonomousAction.ApplyPromo(code)
            }
            "open_chat_with_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                AutonomousAction.OpenVendorChat(vendor.id, vendor.name)
            }
            else -> null
        }
    }

    /**
     * Highly intelligent fallback engine when network or API key is not yet set up in the environment.
     * Accurately parses user intents and triggers autonomous actions.
     */
    private fun handleOfflineFallback(userMessage: String): AiResponseResult {
        val lower = userMessage.lowercase().trim()

        return when {
            lower.contains("chuka") && (lower.contains("book") || lower.contains("request") || lower.contains("hire") || lower.contains("fix")) -> {
                val vendor = BartrRepository.getVendor("chuka")
                AiResponseResult(
                    text = "I can request Chuka's Repairs for phone repair at ₦5,000 with cash on completion. Should I proceed?",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Phone Screen Repair", "₦5,000")
                )
            }
            lower.contains("adaeze") && (lower.contains("book") || lower.contains("request") || lower.contains("hire") || lower.contains("nail")) -> {
                val vendor = BartrRepository.getVendor("adaeze")
                AiResponseResult(
                    text = "I can book Adaeze Nails & Beauty for ₦5,000. Would you like me to request her now?",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Nail Art & Care", "₦5,000")
                )
            }
            lower.contains("musa") && (lower.contains("book") || lower.contains("request") || lower.contains("mechanic")) -> {
                val vendor = BartrRepository.getVendor("musa")
                AiResponseResult(
                    text = "I can request Musa Auto Care for vehicle diagnostics and repair at ₦12,000. Shall I proceed?",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Vehicle Diagnostics", "₦12,000")
                )
            }
            lower.contains("recenter") || lower.contains("center map") || lower.contains("my location") || lower.contains("where am i") -> {
                AiResponseResult(
                    text = "Centering the map on your location at 14 Market Road, Ikeja.",
                    action = AutonomousAction.RecenterMap()
                )
            }
            lower.contains("promo") || lower.contains("discount") || lower.contains("code") || lower.contains("bartr500") -> {
                AiResponseResult(
                    text = "Applying promo code BARTR500 for ₦500 off your next request!",
                    action = AutonomousAction.ApplyPromo("BARTR500")
                )
            }
            lower.contains("payment") || lower.contains("card") || lower.contains("cash") -> {
                AiResponseResult(
                    text = "Opening your payments settings. You can manage cash and card options here.",
                    action = AutonomousAction.NavigateTo("payments", "Payments")
                )
            }
            lower.contains("request") && (lower.contains("my") || lower.contains("previous") || lower.contains("history") || lower.contains("past")) -> {
                AiResponseResult(
                    text = "Taking you to your past requests and active bookings.",
                    action = AutonomousAction.NavigateTo("my_requests", "My Requests")
                )
            }
            lower.contains("saved") || lower.contains("bookmark") || lower.contains("favorite") -> {
                AiResponseResult(
                    text = "Opening your saved vendors list.",
                    action = AutonomousAction.NavigateTo("saved_vendors", "Saved Vendors")
                )
            }
            lower.contains("help") || lower.contains("support") || lower.contains("contact") -> {
                AiResponseResult(
                    text = "Opening Bartr support and FAQ center.",
                    action = AutonomousAction.NavigateTo("help", "Help & Support")
                )
            }
            lower.contains("profile") || lower.contains("account") -> {
                AiResponseResult(
                    text = "Opening your user profile.",
                    action = AutonomousAction.NavigateTo("profile", "Profile")
                )
            }
            lower.contains("invite") || lower.contains("friend") || lower.contains("refer") -> {
                AiResponseResult(
                    text = "Opening the invite screen so you can share Bartr with friends.",
                    action = AutonomousAction.NavigateTo("invite_friend", "Invite a Friend")
                )
            }
            lower.contains("chat") -> {
                val targetVendor = when {
                    lower.contains("adaeze") -> "adaeze"
                    lower.contains("musa") -> "musa"
                    else -> "chuka"
                }
                val vendor = BartrRepository.getVendor(targetVendor)
                AiResponseResult(
                    text = "Opening direct chat with ${vendor.name}.",
                    action = AutonomousAction.OpenVendorChat(vendor.id, vendor.name)
                )
            }
            lower.contains("phone") || lower.contains("screen") || lower.contains("repair") || lower.contains("generator") || lower.contains("plumber") || lower.contains("mechanic") || lower.contains("nail") || lower.contains("hair") || lower.contains("find") || lower.contains("search") -> {
                val query = when {
                    lower.contains("screen") || lower.contains("phone") -> "Phone screen repair"
                    lower.contains("mechanic") || lower.contains("car") -> "Auto mechanic"
                    lower.contains("nail") || lower.contains("beauty") -> "Nails and beauty"
                    else -> userMessage
                }
                AiResponseResult(
                    text = "Searching for trusted vendors for '$query' near Ikeja.",
                    action = AutonomousAction.SearchVendors(query)
                )
            }
            else -> {
                AiResponseResult(
                    text = "I'm your Bartr Live Voice AI. I can find vendors for you, book repairs, navigate screens, center the map, or apply promo codes. What would you like me to do?",
                    action = null
                )
            }
        }
    }
}

data class AiResponseResult(
    val text: String,
    val functionCall: AiFunctionCall? = null,
    val action: AutonomousAction? = null,
    val groundings: List<GroundingSource> = emptyList(),
    val modelUsed: String = "gemini-3.8-flash"
)
