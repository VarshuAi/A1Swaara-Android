package com.varshuai.a1swaara.ui.components

import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.varshuai.a1swaara.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Controller to apply extreme sub-bass boost (1000/1000) and 3D concert spatial acoustics.
 */
@Composable
fun SwaaraBoomAudioEngine(
    audioSessionId: Int,
    isBoomActive: Boolean
) {
    DisposableEffect(audioSessionId, isBoomActive) {
        if (audioSessionId <= 0) return@DisposableEffect onDispose {}

        var bassBoost: BassBoost? = null
        var virtualizer: Virtualizer? = null

        if (isBoomActive) {
            bassBoost = runCatching {
                BassBoost(0, audioSessionId).apply {
                    enabled = true
                    setStrength(1000.toShort())
                }
            }.getOrNull()

            virtualizer = runCatching {
                Virtualizer(0, audioSessionId).apply {
                    enabled = true
                    setStrength(950.toShort())
                }
            }.getOrNull()
        }

        onDispose {
            runCatching { bassBoost?.release() }
            runCatching { virtualizer?.release() }
        }
    }
}

/**
 * Shockwave Bass Aura: Radiates pulsating shockwaves behind the album artwork.
 */
@Composable
fun SwaaraBoomAura(
    isPlaying: Boolean,
    isBoomActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boom_aura_transition")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isPlaying) (if (isBoomActive) 1.24f else 1.10f) else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoomActive) 650 else 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = if (isPlaying) (if (isBoomActive) 1.34f else 1.16f) else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoomActive) 850 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale_2"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isPlaying) (if (isBoomActive) 0.85f else 0.45f) else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoomActive) 700 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // Outer Bass Shockwave Ring
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(pulseScale2)
                    .blur(if (isBoomActive) 32.dp else 20.dp)
                    .background(
                        Brush.radialGradient(
                            colors = if (isBoomActive) listOf(
                                Color(0xFFFF007A).copy(alpha = auraAlpha * 0.7f),
                                Color(0xFF7928CA).copy(alpha = auraAlpha * 0.5f),
                                Color(0xFF00F0FF).copy(alpha = auraAlpha * 0.3f),
                                Color.Transparent
                            ) else listOf(
                                Color(0xFF9D4EDD).copy(alpha = auraAlpha * 0.45f),
                                Color(0xFF00F0FF).copy(alpha = auraAlpha * 0.20f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Inner High-Energy Core Aura
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(pulseScale1)
                    .blur(if (isBoomActive) 22.dp else 14.dp)
                    .background(
                        Brush.radialGradient(
                            colors = if (isBoomActive) listOf(
                                Color(0xFFFF0055).copy(alpha = auraAlpha * 0.9f),
                                Color(0xFF00F5D4).copy(alpha = auraAlpha * 0.6f),
                                Color.Transparent
                            ) else listOf(
                                Color(0xFFE056FD).copy(alpha = auraAlpha * 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
        }
    }
}

/**
 * Dynamic 22-Band Neon Audio Spectrum Visualizer
 */
@Composable
fun SwaaraBoomSpectrum(
    isPlaying: Boolean,
    isBoomActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 22,
    maxHeight: Dp = 26.dp
) {
    val transition = rememberInfiniteTransition(label = "spectrum_transition")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoomActive) 900 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_anim"
    )

    val neonPink = Color(0xFFFF007A)
    val neonCyan = Color(0xFF00F5D4)
    val neonPurple = Color(0xFF7928CA)

    Canvas(modifier = modifier.height(maxHeight)) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barGap = 3.5.dp.toPx()
        val barWidth = ((totalWidth - (barGap * (barCount - 1))) / barCount).coerceAtLeast(3f)

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            val wave1 = sin(progress * 4f + phase) * 0.5f + 0.5f
            val wave2 = sin(progress * 7f - phase * 1.5f) * 0.3f + 0.3f
            val rawFactor = (wave1 * 0.65f + wave2 * 0.35f).coerceIn(0.12f, 1.0f)

            val multiplier = if (isBoomActive) 1.25f else 0.85f
            val activeFactor = if (isPlaying) (rawFactor * multiplier).coerceIn(0.15f, 1.0f) else 0.08f
            val barH = canvasHeight * activeFactor

            val x = i * (barWidth + barGap)
            val y = (canvasHeight - barH) / 2f

            val barBrush = Brush.verticalGradient(
                colors = if (isBoomActive) listOf(neonPink, neonPurple, neonCyan)
                else listOf(neonCyan, neonPurple),
                startY = y,
                endY = y + barH
            )

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Futuristic Cyberpunk "BOOM BASS" HUD Toggle Button
 */
@Composable
fun SwaaraBoomButton(
    isBoomActive: Boolean,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boom_btn_transition")

    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isBoomActive) 1.0f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_glow"
    )

    val neonPink = Color(0xFFFF007A)
    val neonCyan = Color(0xFF00F5D4)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isBoomActive) Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2A0845).copy(alpha = 0.95f),
                        Color(0xFF6441A5).copy(alpha = 0.90f)
                    )
                )
                else Brush.horizontalGradient(
                    listOf(
                        Color(0xFF141422).copy(alpha = 0.85f),
                        Color(0xFF1B1B2C).copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = if (isBoomActive) 1.6.dp else 1.dp,
                brush = if (isBoomActive) Brush.horizontalGradient(
                    listOf(
                        neonPink.copy(alpha = borderGlow),
                        neonCyan.copy(alpha = borderGlow)
                    )
                ) else Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.musical_notes),
                contentDescription = null,
                tint = if (isBoomActive) SwaaraMagenta else SwaaraCyan,
                modifier = Modifier.size(13.dp)
            )

            Text(
                text = if (isBoomActive) "BOOM BASS" else "BASS BOOST",
                color = if (isBoomActive) Color.White else Color.White.copy(alpha = 0.75f),
                fontSize = 11.5.sp,
                fontWeight = if (isBoomActive) FontWeight.Black else FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )

            // Animated mini equalizer indicator inside the button
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(12.dp)
            ) {
                val bar1 by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = if (isBoomActive && isPlaying) 1.0f else 0.4f,
                    animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "mini_bar1"
                )
                val bar2 by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = if (isBoomActive && isPlaying) 1.0f else 0.5f,
                    animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "mini_bar2"
                )
                val bar3 by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = if (isBoomActive && isPlaying) 0.9f else 0.3f,
                    animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "mini_bar3"
                )

                listOf(bar1, bar2, bar3).forEach { factor ->
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((12 * factor).dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isBoomActive) neonCyan else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}
