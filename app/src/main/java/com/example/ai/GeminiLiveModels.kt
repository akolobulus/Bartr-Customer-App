package com.example.ai

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data structures for Gemini Live and REST function calling.
 */
data class AiChatMessage(
    val role: String, // "user", "model", or "function"
    val text: String,
    val functionCall: AiFunctionCall? = null,
    val functionResponse: AiFunctionResponse? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiFunctionCall(
    val name: String,
    val args: Map<String, Any>
)

data class AiFunctionResponse(
    val name: String,
    val response: Map<String, Any>
)

sealed class AutonomousAction(val requiresPermission: Boolean, val summary: String) {
    data class SearchVendors(val query: String, val category: String? = null) :
        AutonomousAction(false, "Search for $query")

    data class NavigateTo(val destination: String, val label: String) :
        AutonomousAction(false, "Navigate to $label")

    data class RecenterMap(val locationName: String = "14 Market Road, Ikeja") :
        AutonomousAction(false, "Center map on current location")

    data class ApplyPromo(val code: String) :
        AutonomousAction(false, "Apply promotional code $code")

    data class OpenVendorChat(val vendorId: String, val vendorName: String) :
        AutonomousAction(false, "Open chat with $vendorName")

    data class BookVendor(
        val vendorId: String,
        val vendorName: String,
        val service: String,
        val price: String
    ) : AutonomousAction(true, "Request $vendorName for $service ($price)")
}

data class PendingPermission(
    val id: String,
    val action: AutonomousAction,
    val title: String,
    val explanation: String,
    val details: Map<String, String> = emptyMap()
)

data class GroundingSource(
    val title: String,
    val uri: String? = null,
    val snippet: String? = null
)
