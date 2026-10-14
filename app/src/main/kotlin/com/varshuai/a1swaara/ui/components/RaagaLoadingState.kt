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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R

@Composable
fun RaagaBrandLoader(
    language: String = "Kannada",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brand_loader")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 6.dp.value,
        targetValue = 28.dp.value,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 14.dp.value,
        targetValue = 36.dp.value,
        animationSpec = infiniteRepeatable(tween(580, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 8.dp.value,
        targetValue = 30.dp.value,
        animationSpec = infiniteRepeatable(tween(490, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 18.dp.value,
        targetValue = 38.dp.value,
        animationSpec = infiniteRepeatable(tween(640, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 10.dp.value,
        targetValue = 24.dp.value,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b5"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Outer Glowing Rotating Ring
        Box(
            modifier = Modifier
                .size(90.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Neon Gradient Border Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
                    .clip(CircleShape)
                    .border(
                        3.dp,
                        Brush.sweepGradient(
                            listOf(
                                RaagaPink,
                                RaagaPurple,
                                RaagaCyan,
                                Color.Transparent,
                                RaagaPink
                            )
                        ),
                        CircleShape
                    )
            )

            // Inner Disc
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(16.dp, CircleShape, ambientColor = RaagaPurple, spotColor = RaagaPink)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1135), Color(0xFF121222))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 5-Bar Dancing Equalizer Waveform
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val bars = listOf(bar1, bar2, bar4, bar3, bar5)
                    bars.forEachIndexed { idx, barH ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barH.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(RaagaCyan, if (idx % 2 == 0) RaagaPink else RaagaPurple)
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Branded Animated Text
        Text(
            text = "Tuning into ${language.replaceFirstChar { it.uppercase() }} Hits...",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Fetching live chartbusters & trending hits",
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun RaagaHomeFeedSkeleton(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Jump Back In Placeholder
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 6 Quick Grid Cards
        val rows = listOf(1, 2, 3)
        rows.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131322).copy(alpha = alpha))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131322).copy(alpha = alpha))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Horizontal Shelf Placeholder
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(175.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF131322).copy(alpha = alpha))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                )
            }
        }
    }
}
