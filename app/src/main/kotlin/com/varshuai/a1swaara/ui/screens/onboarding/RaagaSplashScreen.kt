package com.varshuai.a1swaara.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R
import kotlinx.coroutines.delay

private val SwaaraObsidian = Color(0xFF08080C)
private val SwaaraDeepSurface = Color(0xFF111119)
private val SwaaraMagenta = Color(0xFFFF2DAA)
private val SwaaraViolet = Color(0xFF8B35FF)
private val SwaaraCyan = Color(0xFF20CFFF)
private val SwaaraPrimaryText = Color(0xFFF1F1F5)
private val SwaaraSecondaryText = Color(0xFF8E8EA0)

@Composable
fun RaagaSplashScreen(
    onSplashFinished: (onboardingNeeded: Boolean) -> Unit
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("swaara_prefs", Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        delay(1800)
        onSplashFinished(!onboardingCompleted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SwaaraObsidian),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Neon Backdrop Glow Orbs
        Box(
            modifier = Modifier
                .offset(y = (-30).dp)
                .size(320.dp)
                .shadow(160.dp, CircleShape, ambientColor = SwaaraMagenta, spotColor = SwaaraMagenta)
                .background(SwaaraMagenta.copy(alpha = 0.22f * glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 80.dp, y = 140.dp)
                .size(280.dp)
                .shadow(140.dp, CircleShape, ambientColor = SwaaraViolet, spotColor = SwaaraViolet)
                .background(SwaaraViolet.copy(alpha = 0.25f * glowAlpha), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D Metallic Glowing A1 Swaara Logo Emblem
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(150.dp)
                    .shadow(
                        elevation = 36.dp,
                        shape = RoundedCornerShape(38.dp),
                        ambientColor = Color.Black.copy(alpha = 0.95f),
                        spotColor = SwaaraMagenta.copy(alpha = 0.75f)
                    )
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                SwaaraDeepSurface,
                                Color(0xFF0D0D14),
                                SwaaraObsidian
                            )
                        )
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)),
                        RoundedCornerShape(38.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.a1_swaara_logo),
                    contentDescription = "A1 Swaara Logo",
                    modifier = Modifier
                        .size(118.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Cyber Kerning
            Text(
                text = "A 1   S W A A R A",
                color = SwaaraPrimaryText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "PURE SONIC EXPERIENCE",
                color = SwaaraCyan.copy(alpha = 0.85f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Pulsing Equalizer Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(24.dp)
            ) {
                val waveFactors = listOf(0.4f, 0.8f, 1.0f, 0.6f, 0.9f, 0.5f, 0.75f, 0.35f)
                waveFactors.forEachIndexed { i, f ->
                    val dynamicH = ((f * glowAlpha) * 24).coerceIn(4f, 24f).dp
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(dynamicH)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(SwaaraMagenta, SwaaraCyan)
                                )
                            )
                    )
                }
            }
        }
    }
}
