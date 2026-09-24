package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import com.example.BuildConfig
import kotlin.math.roundToInt
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vendor
import com.example.model.VendorIconType
import com.example.ui.theme.BartrAvocadoT4
import com.example.ui.theme.BartrBackdrop
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrGreenT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrYellow
import com.example.ui.theme.BartrYellowT4
import com.example.ui.theme.RencyFontFamily
import androidx.compose.ui.unit.TextUnit

/**
 * The Bartr Name Logo alone (as depicted in 3.png).
 * Clean, bold typography in the Rency font, perfect for menu bars, headers, and navigation bars.
 */
@Composable
fun BartrNameLogo(
    modifier: Modifier = Modifier,
    color: Color = BartrBlue,
    fontSize: TextUnit = 24.sp,
    letterSpacing: TextUnit = (-0.5).sp,
) {
    Text(
        text = "Bartr",
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = RencyFontFamily,
        letterSpacing = letterSpacing,
        modifier = modifier
    )
}

/**
 * The complete Bartr Text Logo with tagline (as depicted in 2.png).
 * Used for loading/splash screens.
 */
@Composable
fun BartrTextLogoFull(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    tagline: String = "...Let's help you find them.",
    titleSize: TextUnit = 60.sp,
    subtitleSize: TextUnit = 18.sp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bartr",
            color = textColor,
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            fontFamily = RencyFontFamily,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tagline,
            color = textColor,
            fontSize = subtitleSize,
            fontWeight = FontWeight.Normal,
            fontFamily = RencyFontFamily,
            letterSpacing = 0.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BartrLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    topArrowColor: Color = Color.White,
    bottomArrowColor: Color = Color.White.copy(alpha = 0.55f),
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scaleX = w / 32f
        val scaleY = h / 32f

        // Top arrow pointing left-to-right
        val topPath = Path().apply {
            moveTo(6f * scaleX, 12f * scaleY)
            lineTo(14f * scaleX, 6f * scaleY)
            lineTo(14f * scaleX, 10f * scaleY)
            lineTo(26f * scaleX, 10f * scaleY)
            lineTo(26f * scaleX, 14f * scaleY)
            lineTo(14f * scaleX, 14f * scaleY)
            lineTo(14f * scaleX, 18f * scaleY)
            close()
        }
        drawPath(topPath, topArrowColor)

        // Bottom arrow pointing right-to-left
        val bottomPath = Path().apply {
            moveTo(26f * scaleX, 20f * scaleY)
            lineTo(18f * scaleX, 26f * scaleY)
            lineTo(18f * scaleX, 22f * scaleY)
            lineTo(6f * scaleX, 22f * scaleY)
            lineTo(6f * scaleX, 18f * scaleY)
            lineTo(18f * scaleX, 18f * scaleY)
            lineTo(18f * scaleX, 14f * scaleY)
            close()
        }
        drawPath(bottomPath, bottomArrowColor)
    }
}

@Composable
fun BartrTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BartrBackdrop)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = BartrInk,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                color = BartrInk
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun BartrPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BartrBackdrop)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = BartrInk,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = BartrInk
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun BartrButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BartrBlue,
            contentColor = Color.White,
            disabledContainerColor = BartrBlue.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
fun BartrVendorRow(
    vendor: Vendor,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (bgColor, iconColor) = when (vendor.iconType) {
        VendorIconType.REPAIR -> Pair(BartrBlueT4, BartrBlue)
        VendorIconType.BEAUTY -> Pair(BartrGreenT4, Color(0xFF2F9E63))
        VendorIconType.MECHANIC -> Pair(BartrYellowT4, Color(0xFFA68A00))
    }

    val iconVector = when (vendor.iconType) {
        VendorIconType.REPAIR -> Icons.Filled.Build
        VendorIconType.BEAUTY -> Icons.Filled.Spa
        VendorIconType.MECHANIC -> Icons.Filled.DirectionsCar
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = vendor.category,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = vendor.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = BartrInk
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle ?: vendor.category,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BartrInkSoft,
                    fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = vendor.rating,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = BartrBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = vendor.distance,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BartrInkSoft,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun BartrMapView(
    vendors: List<Vendor>,
    selectedVendorId: String? = null,
    onVendorSelected: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
    isMiniMap: Boolean = false,
    trackedVendor: Vendor? = null,
    recenterTrigger: Int = 0,
) {
    val hasValidMapsKey = remember {
        try {
            val key = BuildConfig.MAPS_API_KEY
            key.isNotBlank() && key != "YOUR_MAPS_API_KEY" && !key.startsWith("YOUR_") && key.length > 10
        } catch (e: Exception) {
            false
        }
    }

    if (hasValidMapsKey) {
        BartrGoogleMapImpl(
            vendors = vendors,
            selectedVendorId = selectedVendorId,
            onVendorSelected = onVendorSelected,
            modifier = modifier,
            isMiniMap = isMiniMap,
            trackedVendor = trackedVendor,
            recenterTrigger = recenterTrigger
        )
    } else {
        BartrInteractiveVectorMap(
            vendors = vendors,
            selectedVendorId = selectedVendorId,
            onVendorSelected = onVendorSelected,
            modifier = modifier,
            isMiniMap = isMiniMap,
            trackedVendor = trackedVendor,
            recenterTrigger = recenterTrigger
        )
    }
}

@Composable
fun BartrInteractiveVectorMap(
    vendors: List<Vendor>,
    selectedVendorId: String? = null,
    onVendorSelected: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
    isMiniMap: Boolean = false,
    trackedVendor: Vendor? = null,
    recenterTrigger: Int = 0,
) {
    val userLat = 6.5980
    val userLng = 3.3480

    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var zoomLevel by remember { mutableFloatStateOf(if (isMiniMap) 1.2f else 1.0f) }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            panX = 0f
            panY = 0f
            zoomLevel = if (isMiniMap) 1.2f else 1.0f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vectorPulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicleProgress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9EFEB))
            .pointerInput(isMiniMap) {
                if (!isMiniMap) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panX += dragAmount.x
                        panY += dragAmount.y
                    }
                }
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerX = widthPx / 2f + panX
        val centerY = heightPx / 2f + panY

        // Coordinate scaling: 0.01 deg in Ikeja -> ~240dp on screen
        val coordScale = 24000f * zoomLevel

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Terrain Green Parks
            // Ikeja Golf Club / GRA Park (Northwest)
            drawRoundRect(
                color = Color(0xFFD4E7D6),
                topLeft = Offset(centerX - 240f * zoomLevel, centerY - 280f * zoomLevel),
                size = androidx.compose.ui.geometry.Size(160f * zoomLevel, 140f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            // Ndubuisi Kanu Park / Alausa Green (Northeast)
            drawRoundRect(
                color = Color(0xFFD4E7D6),
                topLeft = Offset(centerX + 110f * zoomLevel, centerY - 260f * zoomLevel),
                size = androidx.compose.ui.geometry.Size(170f * zoomLevel, 120f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )
            // Maryland Recreational Green (South)
            drawRoundRect(
                color = Color(0xFFD8E9DA),
                topLeft = Offset(centerX + 40f * zoomLevel, centerY + 180f * zoomLevel),
                size = androidx.compose.ui.geometry.Size(200f * zoomLevel, 100f * zoomLevel),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )

            // 2. Water Stream / Ikeja Canal
            val waterPath = Path().apply {
                moveTo(centerX - 350f * zoomLevel, centerY + 260f * zoomLevel)
                quadraticTo(
                    centerX - 120f * zoomLevel, centerY + 200f * zoomLevel,
                    centerX + 80f * zoomLevel, centerY + 300f * zoomLevel
                )
                quadraticTo(
                    centerX + 220f * zoomLevel, centerY + 360f * zoomLevel,
                    centerX + 380f * zoomLevel, centerY + 320f * zoomLevel
                )
            }
            drawPath(
                path = waterPath,
                color = Color(0xFFCCE4F0),
                style = Stroke(width = 18f * zoomLevel, cap = StrokeCap.Round)
            )

            // 3. Road Network
            val roadCasing = Color(0xFFD6DDD8)
            val roadSurface = Color(0xFFFFFFFF)
            val secondaryRoad = Color(0xFFF7FAF8)

            // Mobolaji Bank Anthony Way (NW to SE Expressway)
            val mbaPath = Path().apply {
                moveTo(centerX - 320f * zoomLevel, centerY - 320f * zoomLevel)
                lineTo(centerX + 320f * zoomLevel, centerY + 220f * zoomLevel)
            }
            drawPath(mbaPath, roadCasing, style = Stroke(width = 16f * zoomLevel, cap = StrokeCap.Round))
            drawPath(mbaPath, roadSurface, style = Stroke(width = 12f * zoomLevel, cap = StrokeCap.Round))

            // Obafemi Awolowo Way (North to South Arterial)
            val awolowoPath = Path().apply {
                moveTo(centerX - 100f * zoomLevel, centerY - 350f * zoomLevel)
                lineTo(centerX - 60f * zoomLevel, centerY + 350f * zoomLevel)
            }
            drawPath(awolowoPath, roadCasing, style = Stroke(width = 14f * zoomLevel, cap = StrokeCap.Round))
            drawPath(awolowoPath, roadSurface, style = Stroke(width = 10f * zoomLevel, cap = StrokeCap.Round))

            // Allen Avenue (North-South Arterial)
            val allenPath = Path().apply {
                moveTo(centerX + 70f * zoomLevel, centerY - 350f * zoomLevel)
                lineTo(centerX + 90f * zoomLevel, centerY + 350f * zoomLevel)
            }
            drawPath(allenPath, roadCasing, style = Stroke(width = 14f * zoomLevel, cap = StrokeCap.Round))
            drawPath(allenPath, roadSurface, style = Stroke(width = 10f * zoomLevel, cap = StrokeCap.Round))

            // Market Road (West to East Local Road passing user location)
            val marketRdPath = Path().apply {
                moveTo(centerX - 350f * zoomLevel, centerY)
                lineTo(centerX + 350f * zoomLevel, centerY - 15f * zoomLevel)
            }
            drawPath(marketRdPath, roadCasing, style = Stroke(width = 11f * zoomLevel, cap = StrokeCap.Round))
            drawPath(marketRdPath, secondaryRoad, style = Stroke(width = 8f * zoomLevel, cap = StrokeCap.Round))

            // Otigba Street / Computer Village
            val otigbaPath = Path().apply {
                moveTo(centerX - 180f * zoomLevel, centerY - 80f * zoomLevel)
                lineTo(centerX - 160f * zoomLevel, centerY + 120f * zoomLevel)
            }
            drawPath(otigbaPath, roadCasing, style = Stroke(width = 8f * zoomLevel, cap = StrokeCap.Round))
            drawPath(otigbaPath, secondaryRoad, style = Stroke(width = 6f * zoomLevel, cap = StrokeCap.Round))

            // Kodesho Street
            val kodeshoPath = Path().apply {
                moveTo(centerX - 220f * zoomLevel, centerY - 60f * zoomLevel)
                lineTo(centerX + 40f * zoomLevel, centerY - 60f * zoomLevel)
            }
            drawPath(kodeshoPath, roadCasing, style = Stroke(width = 8f * zoomLevel, cap = StrokeCap.Round))
            drawPath(kodeshoPath, secondaryRoad, style = Stroke(width = 6f * zoomLevel, cap = StrokeCap.Round))

            // Aromire Avenue
            val aromirePath = Path().apply {
                moveTo(centerX + 60f * zoomLevel, centerY + 80f * zoomLevel)
                lineTo(centerX + 260f * zoomLevel, centerY + 70f * zoomLevel)
            }
            drawPath(aromirePath, roadCasing, style = Stroke(width = 8f * zoomLevel, cap = StrokeCap.Round))
            drawPath(aromirePath, secondaryRoad, style = Stroke(width = 6f * zoomLevel, cap = StrokeCap.Round))

            // 4. Arriving Transit Polyline in MiniMap Mode
            if (isMiniMap && trackedVendor != null) {
                val vendorPxX = centerX + ((trackedVendor.lng - userLng) * coordScale).toFloat()
                val vendorPxY = centerY - ((trackedVendor.lat - userLat) * coordScale).toFloat()

                // Route shadow
                drawLine(
                    color = BartrBlue.copy(alpha = 0.25f),
                    start = Offset(vendorPxX, vendorPxY),
                    end = Offset(centerX, centerY),
                    strokeWidth = 10f * zoomLevel,
                    cap = StrokeCap.Round
                )
                // Active route line
                drawLine(
                    color = BartrBlue,
                    start = Offset(vendorPxX, vendorPxY),
                    end = Offset(centerX, centerY),
                    strokeWidth = 6f * zoomLevel,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )

                // Dispatch Courier Vehicle moving along route
                val vehicleX = vendorPxX + (centerX - vendorPxX) * vehicleProgress
                val vehicleY = vendorPxY + (centerY - vendorPxY) * vehicleProgress
                drawCircle(
                    color = BartrBlue,
                    radius = 12f * zoomLevel,
                    center = Offset(vehicleX, vehicleY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6f * zoomLevel,
                    center = Offset(vehicleX, vehicleY)
                )
            }
        }

        // 5. User Location Pin (Center: 14 Market Road)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (centerX - 24.dp.toPx()).roundToInt(),
                        (centerY - 24.dp.toPx()).roundToInt()
                    )
                }
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = BartrBlue.copy(alpha = pulseAlpha),
                    radius = pulseAnim,
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 11f,
                    center = center
                )
                drawCircle(
                    color = BartrBlue,
                    radius = 7.5f,
                    center = center
                )
            }
        }

        // User Location Label
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E6E3)),
            modifier = Modifier
                .offset {
                    IntOffset(
                        (centerX - 60.dp.toPx()).roundToInt(),
                        (centerY + 24.dp.toPx()).roundToInt()
                    )
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BartrBlue)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "14 Market Road",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = BartrInk
                    )
                )
            }
        }

        // 6. Interactive Vendor Pins
        if (!isMiniMap) {
            vendors.forEach { vendor ->
                val vX = centerX + ((vendor.lng - userLng) * coordScale).toFloat()
                val vY = centerY - ((vendor.lat - userLat) * coordScale).toFloat()
                val isSelected = vendor.id == selectedVendorId

                val (bgColor, iconVector) = when (vendor.iconType) {
                    VendorIconType.REPAIR -> Pair(BartrBlueS2, Icons.Filled.Build)
                    VendorIconType.BEAUTY -> Pair(Color(0xFF2F9E63), Icons.Filled.Spa)
                    VendorIconType.MECHANIC -> Pair(Color(0xFFC99A00), Icons.Filled.DirectionsCar)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (vX - 22.dp.toPx()).roundToInt(),
                                (vY - 40.dp.toPx()).roundToInt()
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onVendorSelected(vendor) }
                        )
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        shadowElevation = if (isSelected) 10.dp else 5.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.5.dp else 2.dp,
                            color = Color.White
                        ),
                        modifier = Modifier.size(if (isSelected) 44.dp else 38.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = vendor.name,
                                tint = Color.White,
                                modifier = Modifier.size(if (isSelected) 22.dp else 18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) BartrBlue else Color(0xFFE2E6E3)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = vendor.name.split(" ").firstOrNull() ?: vendor.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) BartrBlue else BartrInk
                                )
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "★${vendor.rating}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BartrInkSoft
                                )
                            )
                        }
                    }
                }
            }
        } else if (trackedVendor != null) {
            val tvX = centerX + ((trackedVendor.lng - userLng) * coordScale).toFloat()
            val tvY = centerY - ((trackedVendor.lat - userLat) * coordScale).toFloat()

            val (bgColor, iconVector) = when (trackedVendor.iconType) {
                VendorIconType.REPAIR -> Pair(BartrBlueS2, Icons.Filled.Build)
                VendorIconType.BEAUTY -> Pair(Color(0xFF2F9E63), Icons.Filled.Spa)
                VendorIconType.MECHANIC -> Pair(Color(0xFFC99A00), Icons.Filled.DirectionsCar)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset {
                    IntOffset(
                        (tvX - 22.dp.toPx()).roundToInt(),
                        (tvY - 36.dp.toPx()).roundToInt()
                    )
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(2.5.dp, Color.White),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = trackedVendor.name,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BartrBlue,
                    shadowElevation = 3.dp,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Arriving in 4m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 7. Top Status Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E6E3)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2F9E63))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMiniMap) "Live Tracking • Ikeja" else "Ikeja Local Map",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = BartrInk
                    )
                )
            }
        }

        // 8. Zoom Controls on Right Side (only in main map)
        if (!isMiniMap) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Zoom In",
                            tint = BartrInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Zoom Out",
                            tint = BartrInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BartrGoogleMapImpl(
    vendors: List<Vendor>,
    selectedVendorId: String? = null,
    onVendorSelected: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
    isMiniMap: Boolean = false,
    trackedVendor: Vendor? = null,
    recenterTrigger: Int = 0,
) {
    val userLocation = remember { LatLng(6.5980, 3.3480) } // 14 Market Road, Ikeja, Lagos
    val initialTarget = remember(trackedVendor) {
        if (trackedVendor != null) {
            LatLng(
                (userLocation.latitude + trackedVendor.lat) / 2.0,
                (userLocation.longitude + trackedVendor.lng) / 2.0
            )
        } else {
            userLocation
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, if (isMiniMap) 15.0f else 14.2f)
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation, 15f),
                durationMs = 800
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BartrAvocadoT4)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                rotationGesturesEnabled = !isMiniMap,
                scrollGesturesEnabled = !isMiniMap,
                zoomGesturesEnabled = !isMiniMap,
                tiltGesturesEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            )
        ) {
            // User Location marker (14 Market Road, Ikeja)
            MarkerComposable(
                keys = arrayOf<Any>("user_location", pulseAnim, pulseAlpha),
                state = remember { MarkerState(position = userLocation) },
                title = "Your Location",
                snippet = "14 Market Road, Ikeja"
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = BartrBlue.copy(alpha = pulseAlpha),
                            radius = pulseAnim,
                            center = center
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = center
                        )
                        drawCircle(
                            color = BartrBlue,
                            radius = 6.5f,
                            center = center
                        )
                    }
                }
            }

            // Interactive Vendor markers
            if (!isMiniMap) {
                vendors.forEach { vendor ->
                    val vendorPosition = remember(vendor.id) { LatLng(vendor.lat, vendor.lng) }
                    val isSelected = vendor.id == selectedVendorId
                    val (bgColor, iconVector) = when (vendor.iconType) {
                        VendorIconType.REPAIR -> Pair(BartrBlueS2, Icons.Filled.Build)
                        VendorIconType.BEAUTY -> Pair(Color(0xFF2F9E63), Icons.Filled.Spa)
                        VendorIconType.MECHANIC -> Pair(Color(0xFFC99A00), Icons.Filled.DirectionsCar)
                    }

                    MarkerComposable(
                        keys = arrayOf<Any>(vendor.id, isSelected),
                        state = remember(vendor.id) { MarkerState(position = vendorPosition) },
                        title = vendor.name,
                        snippet = "${vendor.category} • ${vendor.rating}",
                        onClick = {
                            onVendorSelected(vendor)
                            true
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onVendorSelected(vendor) }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                shadowElevation = if (isSelected) 10.dp else 6.dp,
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                                modifier = Modifier.size(if (isSelected) 44.dp else 38.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = vendor.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(if (isSelected) 22.dp else 18.dp)
                                    )
                                }
                            }
                            // Name pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = vendor.name.split(" ").firstOrNull() ?: vendor.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = BartrInk
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else if (trackedVendor != null) {
                // Arriving vendor marker + polyline to user
                val trackedPosition = remember(trackedVendor.id) { LatLng(trackedVendor.lat, trackedVendor.lng) }
                val (bgColor, iconVector) = when (trackedVendor.iconType) {
                    VendorIconType.REPAIR -> Pair(BartrBlueS2, Icons.Filled.Build)
                    VendorIconType.BEAUTY -> Pair(Color(0xFF2F9E63), Icons.Filled.Spa)
                    VendorIconType.MECHANIC -> Pair(Color(0xFFC99A00), Icons.Filled.DirectionsCar)
                }

                Polyline(
                    points = listOf(trackedPosition, userLocation),
                    color = BartrBlue,
                    width = 8f
                )

                MarkerComposable(
                    keys = arrayOf<Any>(trackedVendor.id),
                    state = remember(trackedVendor.id) { MarkerState(position = trackedPosition) },
                    title = trackedVendor.name,
                    snippet = "On the way (${trackedVendor.distance})"
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(2.5.dp, Color.White),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = trackedVendor.name,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BartrButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BartrBackdrop,
            contentColor = BartrInk,
            disabledContainerColor = BartrBackdrop.copy(alpha = 0.5f),
            disabledContentColor = BartrInk.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
fun BartrSpinnerState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showNameLogo: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showNameLogo) {
            BartrNameLogo(
                fontSize = 28.sp,
                color = BartrBlue
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = BartrBlue,
            trackColor = BartrBlueT4,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = BartrInk
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = BartrInkSoft,
                fontSize = 14.5.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
