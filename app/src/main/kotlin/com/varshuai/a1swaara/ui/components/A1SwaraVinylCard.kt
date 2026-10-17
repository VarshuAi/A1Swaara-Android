package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun A1SwaraVinylCard(
    thumbnailUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF1DB954), spotColor = Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Outer Vinyl Disc
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (isPlaying) rotationAngle else 0f)
                .clip(CircleShape)
                .background(Color(0xFF111114))
                .border(2.dp, Color(0xFF22222B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner Vinyl Groove Rings
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .border(1.dp, Color(0xFF1A1A22), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(0.70f)
                    .border(1.dp, Color(0xFF252530), CircleShape)
            )

            // Artwork Center Label
            Box(
                modifier = Modifier
                    .fillMaxSize(0.55f)
                    .clip(CircleShape)
                    .background(Color(0xFF1DB954))
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl != null) {
                    RetryingAsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                // Vinyl Spindle Hole
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF33333E), CircleShape)
                )
            }
        }
    }
}
