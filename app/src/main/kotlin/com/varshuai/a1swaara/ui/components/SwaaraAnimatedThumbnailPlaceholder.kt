package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-fidelity animated loading and fallback placeholder for icons, album art, and avatar thumbnails.
 * Features rotating ambient neon aura with dancing soundwave micro-bars.
 */
@Composable
fun SwaaraAnimatedThumbnailPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
    isCircular: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swaara_thumb_loader")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(540, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(460, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )

    val effectiveShape = if (isCircular) CircleShape else shape

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(effectiveShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0C0C14),
                        Color(0xFF161622),
                        Color(0xFF0F0F1A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Ambient Glow Ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .rotate(rotation)
                .clip(CircleShape)
                .border(
                    1.5.dp,
                    Brush.sweepGradient(
                        listOf(
                            SwaaraMagenta.copy(alpha = 0.8f),
                            SwaaraViolet.copy(alpha = 0.6f),
                            SwaaraCyan.copy(alpha = 0.7f),
                            Color.Transparent,
                            SwaaraMagenta.copy(alpha = 0.8f)
                        )
                    ),
                    CircleShape
                )
        )

        // Center Disc with Soundwave Micro-Bars
        Box(
            modifier = Modifier
                .fillMaxSize(0.68f)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color(0xFF13131F))
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(bar1.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(SwaaraCyan)
                )
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(bar2.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(SwaaraMagenta)
                )
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(bar3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(SwaaraViolet)
                )
            }
        }
    }
}
