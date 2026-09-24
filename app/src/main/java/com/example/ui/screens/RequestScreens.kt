package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BartrRepository
import com.example.model.Vendor
import com.example.ui.components.BartrButtonPrimary
import com.example.ui.components.BartrSpinnerState
import com.example.ui.components.BartrTopBar
import com.example.ui.components.BartrVendorRow
import com.example.ui.theme.BartrBackdrop
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrGreenT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrYellowT4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RequestInputScreen(
    onBack: () -> Unit,
    onFindVendors: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var queryText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var micLabel by remember { mutableStateOf("Tap to speak instead") }
    val coroutineScope = rememberCoroutineScope()

    val pulseTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrTopBar(
            title = "What do you need?",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Describe it in your own words, or tap the mic and just speak. We'll sort out the details for you.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = BartrInkSoft,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large text area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BartrBackdrop)
                    .border(1.5.dp, BartrInk.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                if (queryText.isEmpty()) {
                    Text(
                        text = "e.g. My phone screen cracked, need am fix today",
                        style = TextStyle(
                            color = BartrInkSoft.copy(alpha = 0.7f),
                            fontSize = 15.5.sp,
                            lineHeight = 22.sp
                        )
                    )
                }
                BasicTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = BartrInk,
                        fontSize = 15.5.sp,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(BartrBlue)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Mic simulation row
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(66.dp)
                        .shadow(14.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(BartrBlue)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                if (!isListening) {
                                    isListening = true
                                    micLabel = "Listening..."
                                    coroutineScope.launch {
                                        delay(1300)
                                        queryText = "My phone screen cracked, need am fix today"
                                        isListening = false
                                        micLabel = "Tap to speak instead"
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Speak",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = micLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isListening) BartrBlue else BartrInkSoft,
                        fontWeight = if (isListening) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Bottom fixed action button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            BartrButtonPrimary(
                text = "Find vendors",
                enabled = queryText.trim().isNotEmpty(),
                onClick = { onFindVendors(queryText.trim()) }
            )
        }
    }
}

@Composable
fun ParsedScreen(
    query: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrTopBar(
            title = "Here's what we found",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // User speech bubble
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .background(BartrBackdrop)
                    .padding(18.dp)
            ) {
                Text(
                    text = "\"$query\"",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = BartrInk,
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(BartrBlueT4)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Phone Repair",
                        color = BartrBlueS2,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(BartrYellowT4)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Urgent · Today",
                        color = Color(0xFF8A7A00),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Fair price card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BartrGreenT4)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "A fair price, suggested for you",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF2F7A52),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₦4,500 – ₦6,000",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = BartrInk
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            BartrButtonPrimary(
                text = "Looks good, find vendors",
                onClick = onConfirm
            )
        }
    }
}

@Composable
fun MatchingScreen(
    query: String,
    onMatched: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(1400)
        onMatched()
    }

    BartrSpinnerState(
        title = "Finding vendors near you",
        subtitle = "Matching you with trusted people nearby.",
        modifier = modifier
    )
}

@Composable
fun MatchesScreen(
    onBack: () -> Unit,
    onVendorClick: (Vendor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val matchedVendors = remember {
        listOf(
            BartrRepository.getVendor("chuka"),
            BartrRepository.getVendor("ifeoma"),
            BartrRepository.getVendor("bode"),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BartrTopBar(
            title = "Matched for you",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BEST MATCHES, RANKED FOR YOU",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    letterSpacing = 1.sp,
                    color = BartrInkSoft
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            matchedVendors.forEachIndexed { index, vendor ->
                BartrVendorRow(
                    vendor = vendor,
                    subtitle = "${vendor.category} · ${vendor.response}",
                    onClick = { onVendorClick(vendor) }
                )
                if (index < matchedVendors.lastIndex) {
                    HorizontalDivider(
                        color = BartrInk.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}
