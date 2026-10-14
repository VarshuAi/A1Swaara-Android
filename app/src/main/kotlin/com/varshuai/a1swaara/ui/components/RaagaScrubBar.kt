package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R
import kotlin.math.roundToInt
import kotlin.math.sin

// 1. Sleek A1 Swaara Animated Loading / Play Button
@Composable
fun RaagaPulsingPlayButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 60
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Rotating gradient halo when loading
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Subtle breathing pulse scale when loading
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Bouncing Equalizer Bars for internal loading visualizer
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 9f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .then(if (isLoading) Modifier.scale(pulseScale) else Modifier)
            .shadow(
                elevation = if (isLoading) 16.dp else 12.dp,
                shape = CircleShape,
                ambientColor = SwaaraViolet.copy(alpha = 0.4f),
                spotColor = SwaaraMagenta.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Rotating Neon Sweep Gradient Ring
            Canvas(modifier = Modifier.fillMaxSize().rotate(spinAngle)) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            SwaaraMagenta,
                            SwaaraViolet,
                            SwaaraCyan,
                            Color.Transparent,
                            SwaaraMagenta
                        )
                    ),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Dark Center Disc
            Box(
                modifier = Modifier
                    .size((size - 8).dp)
                    .clip(CircleShape)
                    .background(SwaaraDeepSurface),
                contentAlignment = Alignment.Center
            ) {
                // 3-Bar Equalizer Waveform inside button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(bar1.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SwaaraMagenta)
                    )
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(bar2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SwaaraViolet)
                    )
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(bar3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SwaaraCyan)
                    )
                }
            }
        } else {
            // Solid Gradient Play / Pause State (Magenta -> Violet)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size((size * 0.44f).dp)
                )
            }
        }
    }
}

// 2. Refined A1 Swaara Waveform Scrub Bar
@Composable
fun RaagaFuturisticScrubBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val safeDuration = duration.coerceAtLeast(1L)
    val actualFraction = (currentPosition.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    val displayFraction = if (isDragging) dragFraction else actualFraction

    val formatTime: (Long) -> String = { millis ->
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        "%d:%02d".format(minutes, seconds)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(safeDuration) {
                    detectTapGestures { offset ->
                        val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((newFraction * safeDuration).toLong())
                    }
                }
                .pointerInput(safeDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            onSeek((dragFraction * safeDuration).toLong())
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()

            // 1. Audio Waveform Spectrum Micro-Bars (32 dynamic subtle bars)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                val numBars = 36
                val barWidth = 2.5.dp.toPx()
                val totalBarSpace = size.width
                val spacing = (totalBarSpace - (numBars * barWidth)) / (numBars - 1)

                for (i in 0 until numBars) {
                    val barFraction = i.toFloat() / (numBars - 1)
                    val isPast = barFraction <= displayFraction
                    val waveHeight = ((sin(i * 0.65f) * 0.40f + 0.60f) * size.height * 0.80f).coerceAtLeast(3.dp.toPx())
                    val x = i * (barWidth + spacing)
                    val y = (size.height - waveHeight) / 2f

                    drawRoundRect(
                        brush = if (isPast) {
                            Brush.verticalGradient(listOf(SwaaraMagenta, SwaaraViolet))
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        },
                        topLeft = Offset(x, y),
                        size = Size(barWidth, waveHeight),
                        cornerRadius = CornerRadius(1.5.dp.toPx())
                    )
                }
            }

            // 2. Clean Scrub Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(displayFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(SwaaraMagenta)
                )
            }

            // 3. Compact Bright Thumb Indicator
            val thumbOffsetXPx = (displayFraction * totalWidthPx).coerceIn(0f, totalWidthPx)

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetXPx.roundToInt() - 7.dp.roundToPx(), 0) }
                    .size(14.dp)
                    .shadow(6.dp, CircleShape, ambientColor = SwaaraMagenta, spotColor = SwaaraMagenta)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(SwaaraMagenta)
                )
            }
        }

        // Timestamps (Current Position vs Duration)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(if (isDragging) (dragFraction * safeDuration).toLong() else currentPosition),
                color = if (isDragging) SwaaraMagenta else SwaaraSecondaryText,
                fontSize = 11.5.sp,
                fontWeight = if (isDragging) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = formatTime(safeDuration),
                color = SwaaraSecondaryText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
