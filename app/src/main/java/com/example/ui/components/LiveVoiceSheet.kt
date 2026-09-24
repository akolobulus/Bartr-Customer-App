package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import com.example.ai.GroundingSource
import com.example.ai.PendingPermission
import com.example.ai.VoiceConversationManager
import com.example.ai.VoiceState
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrYellow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LiveVoiceSheet(
    voiceManager: VoiceConversationManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by voiceManager.voiceState.collectAsState()
    val audioRms by voiceManager.audioRms.collectAsState()
    val userTranscript by voiceManager.userTranscript.collectAsState()
    val aiTranscript by voiceManager.aiTranscript.collectAsState()
    val pendingPermission by voiceManager.pendingPermission.collectAsState()
    val isMuted by voiceManager.isMuted.collectAsState()
    val isMicPaused by voiceManager.isMicPaused.collectAsState()
    val groundings by voiceManager.groundings.collectAsState()
    val currentModel by voiceManager.currentModel.collectAsState()
    val liveConnectionInfo by voiceManager.liveConnectionInfo.collectAsState()

    var showKeyboardInput by remember { mutableStateOf(false) }
    var textPrompt by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        voiceManager.startSession()
        onDispose {
            voiceManager.endSession()
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = {
            voiceManager.endSession()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(BartrInkSoft.copy(alpha = 0.3f))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BartrBlue, Color(0xFF7E57C2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Voice Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voice Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BartrInk
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "3.8 LIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE3F2FD)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Place,
                                        contentDescription = "Maps Grounded",
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "MAPS GROUNDED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { voiceManager.toggleMute() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute speaker" else "Mute speaker",
                            tint = if (isMuted) Color.Red else BartrInkSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            voiceManager.endSession()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = BartrInkSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Hands-Free Status Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when {
                    isMicPaused -> Color(0xFFF5F5F5)
                    voiceState == VoiceState.LISTENING -> Color(0xFFE8F5E9)
                    voiceState == VoiceState.SPEAKING -> Color(0xFFE3F2FD)
                    voiceState == VoiceState.THINKING -> Color(0xFFEDE7F6)
                    voiceState == VoiceState.AWAITING_PERMISSION -> Color(0xFFFFF8E1)
                    else -> Color(0xFFF5F5F5)
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = when {
                        isMicPaused -> Color(0xFFBDBDBD)
                        voiceState == VoiceState.LISTENING -> Color(0xFF81C784)
                        voiceState == VoiceState.SPEAKING -> Color(0xFF64B5F6)
                        voiceState == VoiceState.THINKING -> Color(0xFFB39DDB)
                        voiceState == VoiceState.AWAITING_PERMISSION -> Color(0xFFFFB74D)
                        else -> Color(0xFFE0E0E0)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (voiceState == VoiceState.LISTENING && !isMicPaused) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                    }
                    Text(
                        text = when {
                            isMicPaused -> "Mic Paused • Tap mic below to resume"
                            voiceState == VoiceState.LISTENING -> "Live Mic Active • Speak anytime"
                            voiceState == VoiceState.SPEAKING -> "Assistant Speaking • Tap to interrupt"
                            voiceState == VoiceState.THINKING -> "Gemini Processing..."
                            voiceState == VoiceState.AWAITING_PERMISSION -> "Say 'Yes' or 'No' to authorize"
                            else -> "Hands-Free Voice Ready"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isMicPaused -> Color(0xFF616161)
                            voiceState == VoiceState.LISTENING -> Color(0xFF1B5E20)
                            voiceState == VoiceState.SPEAKING -> Color(0xFF0D47A1)
                            voiceState == VoiceState.THINKING -> Color(0xFF4A148C)
                            voiceState == VoiceState.AWAITING_PERMISSION -> Color(0xFFE65100)
                            else -> BartrInk
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Visualizer
            LiveVoiceVisualizer(
                voiceState = voiceState,
                audioRms = audioRms,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transcripts container
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BartrBlueT4.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // User Utterance (if any)
                    if (userTranscript.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BartrBlueS2,
                                modifier = Modifier.padding(start = 24.dp)
                            ) {
                                Text(
                                    text = userTranscript,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // AI Response Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(end = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = aiTranscript,
                                    fontSize = 14.sp,
                                    color = BartrInk,
                                    lineHeight = 20.sp
                                )

                                // Google Maps Grounding Sources Display
                                if (groundings.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Place,
                                                contentDescription = "Maps Grounded",
                                                tint = Color(0xFF1976D2),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Grounded with Google Maps data:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1976D2)
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            groundings.forEach { source ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFE3F2FD)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Place,
                                                            contentDescription = null,
                                                            tint = Color(0xFF1565C0),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = source.title,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFF0D47A1)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Autonomous Action Permission Card
            AnimatedVisibility(
                visible = pendingPermission != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                pendingPermission?.let { permission ->
                    ActionPermissionCard(
                        permission = permission,
                        onApprove = { voiceManager.confirmPendingAction() },
                        onDismiss = { voiceManager.dismissPendingAction() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Voice Action Chips
            Text(
                text = "Try saying or tapping:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BartrInkSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 6.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Find phone repair near me",
                    "Book Chuka for phone repair",
                    "Recenter map on my location",
                    "Open my past requests",
                    "Apply promo code BARTR500",
                    "Open payments options"
                )

                suggestions.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable {
                            voiceManager.submitTextUtterance(suggestion)
                        }
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            color = BartrInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text input fallback toggle
            if (showKeyboardInput) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textPrompt,
                        onValueChange = { textPrompt = it },
                        placeholder = {
                            Text(
                                text = "Ask or tell the AI to do something...",
                                fontSize = 13.sp,
                                color = BartrInkSoft
                            )
                        },
                        textStyle = TextStyle(
                            color = BartrInk,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BartrInk,
                            unfocusedTextColor = BartrInk,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = BartrBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            cursorColor = BartrBlue
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textPrompt.isNotBlank()) {
                                voiceManager.submitTextUtterance(textPrompt)
                                textPrompt = ""
                                focusManager.clearFocus()
                            }
                        })
                    )

                    IconButton(
                        onClick = {
                            if (textPrompt.isNotBlank()) {
                                voiceManager.submitTextUtterance(textPrompt)
                                textPrompt = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(BartrBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom controls: Main Mic Button + Keyboard switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showKeyboardInput = !showKeyboardInput },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = "Toggle Keyboard",
                        tint = BartrInkSoft,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Main Push/Toggle Mic Button with Continuous Hands-Free feedback
                val isListening = voiceState == VoiceState.LISTENING
                val isSpeaking = voiceState == VoiceState.SPEAKING
                val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
                val pulseRingScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseRingScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer pulsing glow when live listening
                        if (isListening && !isMicPaused) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .graphicsLayer {
                                        scaleX = pulseRingScale
                                        scaleY = pulseRingScale
                                        alpha = 0.35f
                                    }
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E88E5))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(elevation = if (isListening) 16.dp else 8.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isMicPaused -> Brush.linearGradient(listOf(Color(0xFF757575), Color(0xFF9E9E9E)))
                                        isListening -> Brush.linearGradient(listOf(Color(0xFF1E88E5), Color(0xFF00ACC1)))
                                        isSpeaking -> Brush.linearGradient(listOf(Color(0xFF00897B), Color(0xFF26A69A)))
                                        else -> Brush.linearGradient(listOf(BartrBlue, Color(0xFF7E57C2)))
                                    }
                                )
                                .clickable {
                                    if (isSpeaking) {
                                        // Tap to interrupt AI speaking
                                        voiceManager.startListening()
                                    } else {
                                        // Toggle pause
                                        voiceManager.toggleMicPause()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMicPaused) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = if (isMicPaused) "Resume microphone" else "Active live microphone",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = when {
                            isMicPaused -> "Mic paused • Tap to resume"
                            isSpeaking -> "Speaking • Tap to interrupt"
                            isListening -> "Live • Speak naturally"
                            else -> "Live • Speak anytime"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isMicPaused) Color(0xFFE53935) else BartrInkSoft
                    )
                }

                // Recenter / Reset button
                IconButton(
                    onClick = {
                        voiceManager.submitTextUtterance("Recenter map on my location")
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Recenter map",
                        tint = BartrBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ActionPermissionCard(
    permission: PendingPermission,
    onApprove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8E1), // Warm amber background
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Permission Needed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "User Permission Required",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB78103)
                    )
                    Text(
                        text = permission.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BartrInk
                    )
                }
            }

            Text(
                text = permission.explanation,
                fontSize = 13.sp,
                color = BartrInkSoft,
                lineHeight = 18.sp
            )

            // Key-Value parameters
            if (permission.details.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        permission.details.forEach { (k, v) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = k,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BartrInkSoft
                                )
                                Text(
                                    text = v,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BartrInk
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Say \"Yes, proceed\" or tap Approve below:",
                fontSize = 11.sp,
                color = BartrInkSoft,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BartrInk)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Decline", fontSize = 13.sp)
                }

                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Approve",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Authorize & Run", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}
