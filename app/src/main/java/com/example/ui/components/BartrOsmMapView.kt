package com.example.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BartrRepository
import com.example.model.Vendor
import com.example.model.VendorIconType
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft

/**
 * Free, fully functional, live OpenStreetMap view using Leaflet.js
 * Works universally without requiring any Google Maps API keys or billing.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BartrOsmMapView(
    vendors: List<Vendor>,
    selectedVendorId: String? = null,
    onVendorSelected: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
    isMiniMap: Boolean = false,
    trackedVendor: Vendor? = null,
    recenterTrigger: Int = 0,
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Handle recenter requests from Voice AI or user tapping recenter button
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && isMapLoaded) {
            webViewRef?.evaluateJavascript("window.recenterMap && window.recenterMap();", null)
        }
    }

    // Handle vendor selection update
    LaunchedEffect(selectedVendorId, isMapLoaded) {
        if (selectedVendorId != null && isMapLoaded) {
            webViewRef?.evaluateJavascript("window.selectVendor && window.selectVendor('$selectedVendorId');", null)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    val mainHandler = Handler(Looper.getMainLooper())

                    class AndroidMapBridge {
                        @JavascriptInterface
                        fun onVendorClicked(vendorId: String) {
                            mainHandler.post {
                                val vendor = BartrRepository.getVendor(vendorId)
                                onVendorSelected(vendor)
                            }
                        }

                        @JavascriptInterface
                        fun onMapReady() {
                            mainHandler.post {
                                isMapLoaded = true
                                if (selectedVendorId != null) {
                                    evaluateJavascript("window.selectVendor && window.selectVendor('$selectedVendorId');", null)
                                }
                            }
                        }
                    }

                    addJavascriptInterface(AndroidMapBridge(), "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                        }
                    }

                    val html = buildOsmMapHtml(
                        vendors = vendors,
                        isMiniMap = isMiniMap,
                        trackedVendor = trackedVendor,
                        selectedVendorId = selectedVendorId
                    )

                    loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            }
        )

        // Map controls on right (Zoom in, Zoom out, Recenter)
        if (!isMiniMap) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recenter location button
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                webViewRef?.evaluateJavascript("window.recenterMap && window.recenterMap();", null)
                            }
                        ),
                    color = Color.White,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Recenter on my location",
                            tint = BartrBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Zoom in button
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                webViewRef?.evaluateJavascript("window.zoomIn && window.zoomIn();", null)
                            }
                        ),
                    color = Color.White,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Zoom in",
                            tint = BartrInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Zoom out button
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                webViewRef?.evaluateJavascript("window.zoomOut && window.zoomOut();", null)
                            }
                        ),
                    color = Color.White,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Zoom out",
                            tint = BartrInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Live Free Map badge at bottom left
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 12.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "OpenStreetMap • Live & Free",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BartrInkSoft
                )
            }
        }
    }
}

/**
 * Builds the self-contained HTML page using Leaflet and free CartoDB/OSM tiles.
 */
private fun buildOsmMapHtml(
    vendors: List<Vendor>,
    isMiniMap: Boolean,
    trackedVendor: Vendor?,
    selectedVendorId: String?
): String {
    val userLat = 6.5980
    val userLng = 3.3480

    val vendorsJson = buildString {
        append("[")
        vendors.forEachIndexed { index, v ->
            if (index > 0) append(",")
            val colorHex = when (v.iconType) {
                VendorIconType.REPAIR -> "#1565C0"
                VendorIconType.BEAUTY -> "#2E7D32"
                VendorIconType.MECHANIC -> "#C99A00"
            }
            val shortName = v.name.split(" ").firstOrNull() ?: v.name
            append("""
                {
                    "id": "${v.id}",
                    "name": "${v.name.replace("\"", "\\\"")}",
                    "shortName": "${shortName.replace("\"", "\\\"")}",
                    "category": "${v.category.replace("\"", "\\\"")}",
                    "rating": "${v.rating.replace("\"", "\\\"")}",
                    "price": "${v.finalPrice.replace("\"", "\\\"")}",
                    "distance": "${v.distance}",
                    "lat": ${v.lat},
                    "lng": ${v.lng},
                    "color": "$colorHex",
                    "initial": "${v.initial}"
                }
            """.trimIndent())
        }
        append("]")
    }

    val trackedJson = if (trackedVendor != null) {
        val colorHex = when (trackedVendor.iconType) {
            VendorIconType.REPAIR -> "#1565C0"
            VendorIconType.BEAUTY -> "#2E7D32"
            VendorIconType.MECHANIC -> "#C99A00"
        }
        """
        {
            "id": "${trackedVendor.id}",
            "name": "${trackedVendor.name.replace("\"", "\\\"")}",
            "lat": ${trackedVendor.lat},
            "lng": ${trackedVendor.lng},
            "color": "$colorHex",
            "distance": "${trackedVendor.distance}"
        }
        """.trimIndent()
    } else "null"

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="file:///android_asset/leaflet/leaflet.css" />
    <script src="file:///android_asset/leaflet/leaflet.js"></script>
    <style>
        html, body, #map {
            width: 100%;
            height: 100%;
            margin: 0;
            padding: 0;
            overflow: hidden;
            background-color: #f1f5f9;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }
        .leaflet-control-attribution {
            display: none !important;
        }
        .leaflet-control-zoom {
            display: none !important;
        }

        /* Pulse user location pin */
        .user-pulse-marker {
            position: relative;
            width: 32px;
            height: 32px;
        }
        .user-pulse-ring {
            position: absolute;
            top: 0;
            left: 0;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: rgba(21, 101, 192, 0.28);
            animation: pulse 2s infinite ease-out;
        }
        .user-pulse-dot {
            position: absolute;
            top: 8px;
            left: 8px;
            width: 16px;
            height: 16px;
            border-radius: 50%;
            background: #1565C0;
            border: 3px solid #ffffff;
            box-shadow: 0 2px 6px rgba(0,0,0,0.35);
        }
        @keyframes pulse {
            0% { transform: scale(0.6); opacity: 1; }
            100% { transform: scale(1.6); opacity: 0; }
        }

        /* Artisan Custom Marker */
        .vendor-pin {
            display: flex;
            flex-direction: column;
            align-items: center;
            cursor: pointer;
            transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        .vendor-pin:active {
            transform: scale(0.92);
        }
        .vendor-badge {
            width: 36px;
            height: 36px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-weight: 700;
            font-size: 15px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.25);
            border: 2px solid #ffffff;
        }
        .vendor-label {
            margin-top: 3px;
            background: #ffffff;
            color: #0f172a;
            padding: 2px 7px;
            border-radius: 6px;
            font-size: 10px;
            font-weight: 700;
            box-shadow: 0 2px 6px rgba(0,0,0,0.18);
            white-space: nowrap;
            display: flex;
            align-items: center;
            gap: 3px;
        }
        .vendor-label span.price {
            color: #1565C0;
            font-weight: 600;
        }
        .selected .vendor-badge {
            transform: scale(1.15);
            box-shadow: 0 0 0 3px #1565C0, 0 8px 16px rgba(0,0,0,0.3);
        }

        /* Popup styling */
        .leaflet-popup-content-wrapper {
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            padding: 4px;
        }
        .leaflet-popup-content {
            margin: 10px 14px;
            line-height: 1.4;
        }
        .popup-title {
            font-weight: 700;
            font-size: 14px;
            color: #0f172a;
            margin-bottom: 2px;
        }
        .popup-sub {
            font-size: 11px;
            color: #64748b;
            margin-bottom: 8px;
        }
        .popup-btn {
            display: block;
            width: 100%;
            text-align: center;
            background: #1565C0;
            color: #ffffff;
            font-size: 12px;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 8px;
            text-decoration: none;
            box-sizing: border-box;
        }

        /* Route animated vehicle */
        .vehicle-marker {
            width: 32px;
            height: 32px;
            background: #1565C0;
            color: #ffffff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            border: 2px solid #ffffff;
            box-shadow: 0 4px 10px rgba(0,0,0,0.3);
        }
    </style>
</head>
<body>
    <div id="map"></div>

    <script>
        const userCoords = [$userLat, $userLng];
        const vendorsData = $vendorsJson;
        const trackedData = $trackedJson;
        const isMiniMap = $isMiniMap;

        // Initialize Leaflet map
        const initialZoom = isMiniMap ? 14 : 14;
        const map = L.map('map', {
            center: userCoords,
            zoom: initialZoom,
            zoomControl: false,
            attributionControl: false
        });

        // Add CartoDB Voyager tiles (100% free, fast, clean vector look)
        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
            subdomains: 'abcd',
            maxZoom: 19,
            errorTileUrl: 'https://tile.openstreetmap.org/14/9248/8040.png'
        }).addTo(map);

        // Fallback tile layer (standard OpenStreetMap) if CartoDB experiences hiccups
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            opacity: 0.05
        }).addTo(map);

        // User Location Pin
        const userIcon = L.divIcon({
            className: 'user-pulse-container',
            html: '<div class="user-pulse-marker"><div class="user-pulse-ring"></div><div class="user-pulse-dot"></div></div>',
            iconSize: [32, 32],
            iconAnchor: [16, 16]
        });

        const userMarker = L.marker(userCoords, { icon: userIcon })
            .addTo(map)
            .bindPopup('<div class="popup-title">Your Location</div><div class="popup-sub">14 Market Road, Ikeja</div>');

        const vendorMarkers = {};

        // Add vendors if full map
        if (!isMiniMap) {
            vendorsData.forEach(v => {
                const iconHtml = `
                    <div class="vendor-pin" id="pin-${'$'}{v.id}">
                        <div class="vendor-badge" style="background-color: ${'$'}{v.color}">
                            ${'$'}{v.initial}
                        </div>
                        <div class="vendor-label">
                            <span>${'$'}{v.shortName}</span>
                            <span class="price">${'$'}{v.price}</span>
                        </div>
                    </div>
                `;

                const markerIcon = L.divIcon({
                    className: 'custom-vendor-marker',
                    html: iconHtml,
                    iconSize: [60, 54],
                    iconAnchor: [30, 27]
                });

                const marker = L.marker([v.lat, v.lng], { icon: markerIcon }).addTo(map);
                
                const popupContent = `
                    <div class="popup-title">${'$'}{v.name}</div>
                    <div class="popup-sub">${'$'}{v.category} • ${'$'}{v.rating} • ${'$'}{v.distance}</div>
                    <a href="javascript:void(0)" class="popup-btn" onclick="onVendorSelected('${'$'}{v.id}')">Select & Request</a>
                `;
                marker.bindPopup(popupContent);

                marker.on('click', () => {
                    onVendorSelected(v.id);
                });

                vendorMarkers[v.id] = marker;
            });
        }

        // MiniMap tracking mode (route from vendor to user)
        if (isMiniMap && trackedData) {
            const vendorCoords = [trackedData.lat, trackedData.lng];
            
            // Draw polyline route
            const routeLine = L.polyline([vendorCoords, userCoords], {
                color: '#1565C0',
                weight: 5,
                opacity: 0.85,
                dashArray: '8, 8'
            }).addTo(map);

            // Add vendor marker
            const vendorIcon = L.divIcon({
                className: 'custom-vendor-marker',
                html: `<div class="vehicle-marker">🚗</div>`,
                iconSize: [32, 32],
                iconAnchor: [16, 16]
            });
            L.marker(vendorCoords, { icon: vendorIcon })
                .addTo(map)
                .bindPopup(`<div class="popup-title">${'$'}{trackedData.name}</div><div class="popup-sub">On the way (${'$'}{trackedData.distance})</div>`);

            // Fit bounds to show both user and vendor
            const bounds = L.latLngBounds([userCoords, vendorCoords]);
            map.fitBounds(bounds, { padding: [40, 40] });
        }

        // JS methods called from Android
        window.recenterMap = function() {
            map.flyTo(userCoords, 15, { duration: 0.8 });
            userMarker.openPopup();
        };

        window.zoomIn = function() {
            map.zoomIn();
        };

        window.zoomOut = function() {
            map.zoomOut();
        };

        window.selectVendor = function(id) {
            const marker = vendorMarkers[id];
            if (marker) {
                map.flyTo(marker.getLatLng(), 15.5, { duration: 0.8 });
                marker.openPopup();
            }
        };

        function onVendorSelected(id) {
            if (window.AndroidBridge) {
                window.AndroidBridge.onVendorClicked(id);
            }
        }

        // Notify Android bridge that map is ready
        setTimeout(() => {
            if (window.AndroidBridge && window.AndroidBridge.onMapReady) {
                window.AndroidBridge.onMapReady();
            }
        }, 300);
    </script>
</body>
</html>
    """.trimIndent()
}
