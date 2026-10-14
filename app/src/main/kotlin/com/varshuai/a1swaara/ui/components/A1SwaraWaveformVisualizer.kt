package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun A1SwaraWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 12,
    barWidth: Dp = 4.dp,
    barGap: Dp = 3.dp,
    maxHeight: Dp = 32.dp,
    color: Color = Color(0xFF1DB954)
) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")

    val heights = List(barCount) { index ->
        val duration = 400 + (index * 120) % 500
        val targetVal = 0.3f + ((index * 7) % 7) * 0.1f
        val animatedHeight by transition.animateFloat(
            initialValue = 0.15f,
            targetValue = if (isPlaying) targetVal else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
        animatedHeight
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { heightFactor ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(if (isPlaying) heightFactor else 0.15f)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}
