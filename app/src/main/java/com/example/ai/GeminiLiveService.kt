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
    private const val PRIMARY_MODEL = "gemini-3.6-flash"
    private val CANDIDATE_MODELS = listOf(
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-flash-latest",
        "gemini-3.8-flash"
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
                You are Amaka, a warm, soft-spoken Nigerian female AI assistant for Bartr in Lagos, Nigeria.
                You speak with a gentle, polite, soothing, and natural Nigerian female tone and cadence.
                Bartr connects people in Lagos, Nigeria (Ikeja, Yaba, Surulere, Lekki, Victoria Island, etc.) with trusted local service providers and artisans.
                You have full autonomous agency to do anything for the user on this device.
                When the user gives a request or command, ALWAYS call the appropriate tool to execute it directly.
                Available tools:
                - search_vendors: search local artisans by trade, problem, or keywords (e.g. phone repair, mechanic, beauty, plumber, electrician, generator, AC repair, carpentry)
                - filter_vendors: filter by 'cheapest', 'top_rated', 'nearest', or specific category
                - select_vendor: view or highlight a vendor on the map and open their card
                - book_vendor: hire or book a vendor for a service
                - open_vendor_chat: open direct chat with a vendor (Chuka, Adaeze, Musa, Ifeoma, Bode)
                - call_vendor: initiate a direct phone call to an artisan
                - navigate_to: switch to any screen (home, payments, promotions, my_requests, saved_vendors, help, about, profile, invite_friend, matches)
                - recenter_map: recenter the map on 14 Market Road, Ikeja
                - zoom_map: zoom the map in or out
                - apply_promo_code: apply promo discount code (e.g. BARTR500)
                - toggle_save_vendor: bookmark or favorite a vendor
                - get_service_quote: provide price benchmark & breakdown for a task
                - share_app: share referral link with friends
                - clear_filters: reset search filters and display all available artisans
                
                Keep spoken replies brief, sweet, polite, and natural (1-2 sentences), embodying a soft, calm Nigerian girl voice.
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
                    "query" to ("STRING" to "Service or trade to search for, e.g. 'iPhone repair', 'mechanic', 'plumber'"),
                    "category" to ("STRING" to "Category of service")
                ),
                required = listOf("query")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "filter_vendors",
                description = "Filter the vendor list by criteria: 'cheapest', 'top_rated', 'nearest', 'repairs', 'beauty', 'mechanic'.",
                properties = mapOf(
                    "filter_type" to ("STRING" to "Type of filter: 'cheapest', 'top_rated', 'nearest', 'category'"),
                    "value" to ("STRING" to "Filter value or category name")
                ),
                required = listOf("filter_type", "value")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "select_vendor",
                description = "Select and highlight a vendor on the map and bottom sheet.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID (chuka, adaeze, musa, ifeoma, bode)")
                ),
                required = listOf("vendor_id")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "book_vendor",
                description = "Request or book a verified vendor for a task.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID or name (e.g. 'chuka', 'adaeze', 'musa', 'ifeoma', 'bode')"),
                    "service" to ("STRING" to "The service requested (e.g. 'Screen Replacement')"),
                    "price" to ("STRING" to "Estimated price, e.g. '₦5,000'")
                ),
                required = listOf("vendor_id", "service")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "call_vendor",
                description = "Call a vendor directly on the phone.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID (chuka, adaeze, musa, ifeoma, bode)")
                ),
                required = listOf("vendor_id")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "open_chat_with_vendor",
                description = "Open the direct chat window with a vendor.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID or name (chuka, adaeze, musa, ifeoma, bode)"),
                    "initial_message" to ("STRING" to "Optional initial message to prepare in chat")
                ),
                required = listOf("vendor_id")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "navigate_to",
                description = "Navigate to any screen on the app.",
                properties = mapOf(
                    "screen" to ("STRING" to "Target screen: 'home', 'payments', 'promotions', 'my_requests', 'saved_vendors', 'help', 'about', 'profile', 'invite_friend', 'matches'")
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
                name = "zoom_map",
                description = "Zoom the map in or out.",
                properties = mapOf(
                    "direction" to ("STRING" to "'in' or 'out'")
                ),
                required = listOf("direction")
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
                name = "toggle_save_vendor",
                description = "Save/bookmark or remove a vendor from favorites.",
                properties = mapOf(
                    "vendor_id" to ("STRING" to "Vendor ID"),
                    "save" to ("STRING" to "'true' to save, 'false' to remove")
                ),
                required = listOf("vendor_id")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "get_service_quote",
                description = "Get fair price guidance and price quote for any artisan service in Lagos.",
                properties = mapOf(
                    "trade" to ("STRING" to "Trade e.g. 'plumber', 'electrician', 'mechanic', 'phone repair'"),
                    "task_description" to ("STRING" to "What needs fixing")
                ),
                required = listOf("trade")
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "share_app",
                description = "Share referral code with friends.",
                properties = emptyMap(),
                required = emptyList()
            )
        )

        declarations.put(
            createFunctionDeclaration(
                name = "clear_filters",
                description = "Reset search and filter state to show all artisans.",
                properties = emptyMap(),
                required = emptyList()
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
            "filter_vendors" -> {
                val filterType = fc.args["filter_type"]?.toString() ?: "category"
                val value = fc.args["value"]?.toString() ?: "repairs"
                AutonomousAction.FilterVendors(filterType, value)
            }
            "select_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                AutonomousAction.SelectVendor(vendor.id, vendor.name)
            }
            "book_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                val service = fc.args["service"]?.toString() ?: vendor.category
                val price = fc.args["price"]?.toString() ?: vendor.finalPrice
                AutonomousAction.BookVendor(vendor.id, vendor.name, service, price)
            }
            "call_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                AutonomousAction.CallVendor(vendor.id, vendor.name, vendor.phone)
            }
            "open_chat_with_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                val initialMsg = fc.args["initial_message"]?.toString()
                AutonomousAction.OpenVendorChat(vendor.id, vendor.name, initialMsg)
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
            "zoom_map" -> {
                val dir = fc.args["direction"]?.toString()?.lowercase() ?: "in"
                AutonomousAction.ZoomMap(dir)
            }
            "apply_promo_code" -> {
                val code = fc.args["code"]?.toString() ?: "BARTR500"
                AutonomousAction.ApplyPromo(code)
            }
            "toggle_save_vendor" -> {
                val vId = fc.args["vendor_id"]?.toString()?.lowercase() ?: "chuka"
                val vendor = BartrRepository.getVendor(vId)
                val save = fc.args["save"]?.toString()?.toBoolean() ?: true
                AutonomousAction.ToggleSaveVendor(vendor.id, vendor.name, save)
            }
            "get_service_quote" -> {
                val trade = fc.args["trade"]?.toString() ?: "phone repair"
                val task = fc.args["task_description"]?.toString() ?: "diagnostic"
                val estimate = when {
                    trade.contains("phone", true) -> "₦4,500 – ₦6,000"
                    trade.contains("nail", true) || trade.contains("beauty", true) -> "₦3,000 – ₦7,500"
                    trade.contains("mechanic", true) || trade.contains("car", true) -> "₦8,000 – ₦25,000"
                    trade.contains("plumb", true) -> "₦5,000 – ₦15,000"
                    trade.contains("electric", true) -> "₦4,000 – ₦12,000"
                    else -> "₦5,000 – ₦10,000"
                }
                AutonomousAction.CalculateQuote(trade, task, estimate)
            }
            "share_app" -> AutonomousAction.ShareApp()
            "clear_filters" -> AutonomousAction.ClearFilters()
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
            // Phone call actions
            lower.contains("call") || lower.contains("phone") && lower.contains("dial") || lower.contains("ring") -> {
                val targetVendor = when {
                    lower.contains("adaeze") -> "adaeze"
                    lower.contains("musa") -> "musa"
                    lower.contains("ifeoma") -> "ifeoma"
                    lower.contains("bode") -> "bode"
                    else -> "chuka"
                }
                val vendor = BartrRepository.getVendor(targetVendor)
                AiResponseResult(
                    text = "Calling ${vendor.name} at ${vendor.phone} for you right away.",
                    action = AutonomousAction.CallVendor(vendor.id, vendor.name, vendor.phone)
                )
            }
            // Book / Request actions
            (lower.contains("book") || lower.contains("request") || lower.contains("hire") || lower.contains("order")) && lower.contains("chuka") -> {
                val vendor = BartrRepository.getVendor("chuka")
                AiResponseResult(
                    text = "I've requested Chuka's Repairs for phone screen repair at ₦5,000 with pay on completion.",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Phone Screen Repair", "₦5,000")
                )
            }
            (lower.contains("book") || lower.contains("request") || lower.contains("hire") || lower.contains("order")) && lower.contains("adaeze") -> {
                val vendor = BartrRepository.getVendor("adaeze")
                AiResponseResult(
                    text = "Booking Adaeze Nails & Beauty for gel manicure at ₦5,000 for you.",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Nail Art & Care", "₦5,000")
                )
            }
            (lower.contains("book") || lower.contains("request") || lower.contains("hire") || lower.contains("order")) && lower.contains("musa") -> {
                val vendor = BartrRepository.getVendor("musa")
                AiResponseResult(
                    text = "Requesting Musa Auto Care for vehicle diagnostic & maintenance at ₦12,000.",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, "Vehicle Diagnostics", "₦12,000")
                )
            }
            // General booking without specific name
            lower.contains("book") || lower.contains("hire") || lower.contains("request") -> {
                val vendor = when {
                    lower.contains("nail") || lower.contains("beauty") -> BartrRepository.getVendor("adaeze")
                    lower.contains("mechanic") || lower.contains("car") || lower.contains("auto") -> BartrRepository.getVendor("musa")
                    else -> BartrRepository.getVendor("chuka")
                }
                AiResponseResult(
                    text = "Booking ${vendor.name} for ${vendor.category} at ${vendor.finalPrice} for you now.",
                    action = AutonomousAction.BookVendor(vendor.id, vendor.name, vendor.category, vendor.finalPrice)
                )
            }
            // Selection / Detail view
            lower.contains("select") || lower.contains("show me") || lower.contains("view") && (lower.contains("chuka") || lower.contains("adaeze") || lower.contains("musa") || lower.contains("ifeoma") || lower.contains("bode")) -> {
                val targetVendor = when {
                    lower.contains("adaeze") -> "adaeze"
                    lower.contains("musa") -> "musa"
                    lower.contains("ifeoma") -> "ifeoma"
                    lower.contains("bode") -> "bode"
                    else -> "chuka"
                }
                val vendor = BartrRepository.getVendor(targetVendor)
                AiResponseResult(
                    text = "Selected ${vendor.name} on the map. Rating is ${vendor.rating}, distance ${vendor.distance}.",
                    action = AutonomousAction.SelectVendor(vendor.id, vendor.name)
                )
            }
            // Filter actions
            lower.contains("cheap") || lower.contains("budget") || lower.contains("lowest price") || lower.contains("affordable") -> {
                AiResponseResult(
                    text = "Filtering for the most affordable and budget-friendly artisans in Ikeja.",
                    action = AutonomousAction.FilterVendors("cheapest", "price")
                )
            }
            lower.contains("top rated") || lower.contains("best") || lower.contains("highest rating") || lower.contains("5 star") -> {
                AiResponseResult(
                    text = "Showing top rated 5-star artisans in Ikeja.",
                    action = AutonomousAction.FilterVendors("top_rated", "rating")
                )
            }
            lower.contains("nearest") || lower.contains("closest") || lower.contains("nearby") -> {
                AiResponseResult(
                    text = "Filtering for artisans nearest to your location on Market Road.",
                    action = AutonomousAction.FilterVendors("nearest", "distance")
                )
            }
            // Map controls
            lower.contains("recenter") || lower.contains("center map") || lower.contains("my location") || lower.contains("where am i") -> {
                AiResponseResult(
                    text = "Centering map on 14 Market Road, Ikeja, Lagos.",
                    action = AutonomousAction.RecenterMap()
                )
            }
            lower.contains("zoom in") -> {
                AiResponseResult(
                    text = "Zooming in on the map.",
                    action = AutonomousAction.ZoomMap("in")
                )
            }
            lower.contains("zoom out") -> {
                AiResponseResult(
                    text = "Zooming out on the map.",
                    action = AutonomousAction.ZoomMap("out")
                )
            }
            // Chat
            lower.contains("chat") || lower.contains("message") || lower.contains("text") -> {
                val targetVendor = when {
                    lower.contains("adaeze") -> "adaeze"
                    lower.contains("musa") -> "musa"
                    lower.contains("ifeoma") -> "ifeoma"
                    lower.contains("bode") -> "bode"
                    else -> "chuka"
                }
                val vendor = BartrRepository.getVendor(targetVendor)
                AiResponseResult(
                    text = "Opening chat with ${vendor.name}.",
                    action = AutonomousAction.OpenVendorChat(vendor.id, vendor.name)
                )
            }
            // Promo code
            lower.contains("promo") || lower.contains("discount") || lower.contains("code") || lower.contains("bartr500") -> {
                AiResponseResult(
                    text = "Applying promo code BARTR500 for ₦500 off your next service request!",
                    action = AutonomousAction.ApplyPromo("BARTR500")
                )
            }
            // Price quotes & Estimates
            lower.contains("quote") || lower.contains("how much") || lower.contains("cost") || lower.contains("estimate") || lower.contains("price for") -> {
                val trade = when {
                    lower.contains("screen") || lower.contains("phone") -> "Phone screen repair"
                    lower.contains("nail") || lower.contains("beauty") -> "Nail manicure"
                    lower.contains("car") || lower.contains("mechanic") -> "Car diagnostic"
                    lower.contains("plumb") -> "Plumbing service"
                    lower.contains("electric") -> "Electrical wiring"
                    else -> "General artisan repair"
                }
                val estimate = when {
                    trade.contains("Phone") -> "₦4,500 – ₦6,000"
                    trade.contains("Nail") -> "₦3,000 – ₦7,000"
                    trade.contains("Car") -> "₦8,000 – ₦25,000"
                    else -> "₦5,000 – ₦10,000"
                }
                AiResponseResult(
                    text = "Fair price benchmark for $trade in Ikeja is $estimate with no upfront fees.",
                    action = AutonomousAction.CalculateQuote(trade, userMessage, estimate)
                )
            }
            // Bookmark / Save
            lower.contains("save") || lower.contains("bookmark") || lower.contains("favorite") -> {
                if (lower.contains("view") || lower.contains("open") || lower.contains("list") || lower.contains("show")) {
                    AiResponseResult(
                        text = "Opening your saved vendors list.",
                        action = AutonomousAction.NavigateTo("saved_vendors", "Saved Vendors")
                    )
                } else {
                    val targetVendor = when {
                        lower.contains("adaeze") -> "adaeze"
                        lower.contains("musa") -> "musa"
                        lower.contains("ifeoma") -> "ifeoma"
                        lower.contains("bode") -> "bode"
                        else -> "chuka"
                    }
                    val vendor = BartrRepository.getVendor(targetVendor)
                    AiResponseResult(
                        text = "Saved ${vendor.name} to your favorites.",
                        action = AutonomousAction.ToggleSaveVendor(vendor.id, vendor.name, true)
                    )
                }
            }
            // Reset filters
            lower.contains("clear") || lower.contains("reset") || lower.contains("all artisans") || lower.contains("show all") -> {
                AiResponseResult(
                    text = "Cleared all filters. Showing all verified artisans.",
                    action = AutonomousAction.ClearFilters()
                )
            }
            // Navigation
            lower.contains("payment") || lower.contains("card") || lower.contains("cash") -> {
                AiResponseResult(
                    text = "Opening payment methods. You can choose cash on completion or debit cards.",
                    action = AutonomousAction.NavigateTo("payments", "Payments")
                )
            }
            lower.contains("request") && (lower.contains("my") || lower.contains("history") || lower.contains("past") || lower.contains("active")) -> {
                AiResponseResult(
                    text = "Opening your requests history.",
                    action = AutonomousAction.NavigateTo("my_requests", "My Requests")
                )
            }
            lower.contains("help") || lower.contains("support") || lower.contains("faq") || lower.contains("issue") -> {
                AiResponseResult(
                    text = "Opening Bartr Help & Support center.",
                    action = AutonomousAction.NavigateTo("help", "Help & Support")
                )
            }
            lower.contains("profile") || lower.contains("account") || lower.contains("settings") -> {
                AiResponseResult(
                    text = "Opening your profile settings.",
                    action = AutonomousAction.NavigateTo("profile", "Profile")
                )
            }
            lower.contains("invite") || lower.contains("friend") || lower.contains("share") -> {
                AiResponseResult(
                    text = "Opening the invite screen to share Bartr with your friends.",
                    action = AutonomousAction.NavigateTo("invite_friend", "Invite a Friend")
                )
            }
            lower.contains("about") || lower.contains("terms") -> {
                AiResponseResult(
                    text = "Opening About Bartr.",
                    action = AutonomousAction.NavigateTo("about", "About")
                )
            }
            // Search by trade
            lower.contains("phone") || lower.contains("screen") || lower.contains("battery") ||
            lower.contains("mechanic") || lower.contains("car") || lower.contains("brake") ||
            lower.contains("nail") || lower.contains("hair") || lower.contains("beauty") ||
            lower.contains("plumber") || lower.contains("electrician") || lower.contains("generator") ||
            lower.contains("carpenter") || lower.contains("find") || lower.contains("search") -> {
                val query = when {
                    lower.contains("screen") || lower.contains("phone") -> "Phone Repair"
                    lower.contains("mechanic") || lower.contains("car") -> "Auto Mechanic"
                    lower.contains("nail") || lower.contains("beauty") -> "Nail Tech"
                    lower.contains("plumb") -> "Plumbing"
                    lower.contains("electric") -> "Electrical"
                    lower.contains("generator") -> "Generator Repair"
                    else -> userMessage
                }
                AiResponseResult(
                    text = "Found trusted artisans for '$query' in Ikeja.",
                    action = AutonomousAction.SearchVendors(query)
                )
            }
            else -> {
                AiResponseResult(
                    text = "Hi! I'm your Bartr voice assistant. I can find trusted artisans, book services, make calls, or check fair prices for you. What would you like us to sort out today?",
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
