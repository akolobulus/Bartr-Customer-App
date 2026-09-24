package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.VoiceState
import com.example.ui.theme.BartrBlue
import com.example.ui.theme.BartrBlueS2
import com.example.ui.theme.BartrBlueT4
import com.example.ui.theme.BartrInk
import com.example.ui.theme.BartrInkSoft
import com.example.ui.theme.BartrYellow
import kotlin.math.sin

@Composable
fun LiveVoiceVisualizer(
    voiceState: VoiceState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "VoiceVisualizerTransition")

    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val activeColor = when (voiceState) {
        VoiceState.LISTENING -> Color(0xFF1E88E5) // Electric Blue
        VoiceState.THINKING -> Color(0xFF7E57C2) // Violet / AI
        VoiceState.SPEAKING -> Color(0xFF00897B) // Teal
        VoiceState.AWAITING_PERMISSION -> Color(0xFFE65100) // Deep Orange
        VoiceState.IDLE -> BartrBlueS2.copy(alpha = 0.6f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Concentric glowing orb
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val effectiveScale = if (voiceState == VoiceState.LISTENING) {
                    pulseScale + (audioRms * 0.45f)
                } else if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.THINKING) {
                    pulseScale
                } else {
                    1.0f
                }

                // Outer ambient glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.28f),
                            activeColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * effectiveScale * 1.35f
                    ),
                    radius = radius * effectiveScale * 1.35f,
                    center = center
                )

                // Mid ring
                drawCircle(
                    color = activeColor.copy(alpha = 0.35f),
                    radius = radius * effectiveScale * 0.85f,
                    center = center
                )

                // Core sphere
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            activeColor,
                            activeColor.copy(alpha = 0.85f)
                        ),
                        start = Offset(center.x - radius, center.y - radius),
                        end = Offset(center.x + radius, center.y + radius)
                    ),
                    radius = radius * 0.55f,
                    center = center
                )
            }

            // Central dancing audio waveform bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barCount = 5
                for (i in 0 until barCount) {
                    val factor = sin(wavePhase + (i * 0.8f))
                    val dynamicHeight = if (voiceState == VoiceState.LISTENING) {
                        (14 + (audioRms * 32f) * (0.5f + 0.5f * factor)).coerceIn(8f, 44f).dp
                    } else if (voiceState == VoiceState.SPEAKING) {
                        (12 + (18f * (0.5f + 0.5f * factor))).coerceIn(8f, 32f).dp
                    } else if (voiceState == VoiceState.THINKING) {
                        (8 + (10f * (0.5f + 0.5f * factor))).dp
                    } else {
                        8.dp
                    }

                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(dynamicHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator label
        val stateText = when (voiceState) {
            VoiceState.LISTENING -> "Listening... speak now"
            VoiceState.THINKING -> "Gemini is thinking..."
            VoiceState.SPEAKING -> "Gemini is speaking..."
            VoiceState.AWAITING_PERMISSION -> "Requires your authorization"
            VoiceState.IDLE -> "Tap mic to speak with AI"
        }

        Text(
            text = stateText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = activeColor
            )
        )
    }
}
