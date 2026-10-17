package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.Player
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.player.SwaaraLoopCounter

/**
 * High-precision tactile loop counter modal designed with UI/UX Pro Max principles.
 * Features animated stepper buttons (+ / -), quick preset pills, and glowing glassmorphic aesthetics.
 */
@Composable
fun SwaaraLoopCounterDialog(
    player: Player?,
    onDismiss: () -> Unit
) {
    val initialCount = SwaaraLoopCounter.remainingCount.value
    var selectedCount by remember { mutableIntStateOf(if (initialCount > 0) initialCount else 3) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = SwaaraMagenta.copy(alpha = 0.40f),
                    ambientColor = Color.Black
                )
                .clip(RoundedCornerShape(32.dp))
                .background(SwaaraDeepSurface)
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            SwaaraMagenta.copy(alpha = 0.55f),
                            Color(0xFF7928CA).copy(alpha = 0.25f),
                            SwaaraCyan.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SwaaraMagenta.copy(alpha = 0.22f),
                                    SwaaraCyan.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            SwaaraMagenta.copy(alpha = 0.40f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sync),
                        contentDescription = "Loop Counter",
                        tint = SwaaraMagenta,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Custom Loop Repeater",
                    color = SwaaraPrimaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Set how many times this song should loop",
                    color = SwaaraSecondaryText,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tactile Stepper Unit (- [Count] +)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minus Button
                    StepperButton(
                        text = "-",
                        enabled = selectedCount > 1,
                        isPrimary = false,
                        onClick = {
                            if (selectedCount > 1) selectedCount--
                        }
                    )

                    // Counter Display with Animated Number Flip
                    Box(
                        modifier = Modifier
                            .height(84.dp)
                            .width(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SwaaraElevatedSurface)
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        SwaaraMagenta.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                ),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimatedContent(
                                targetState = selectedCount,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                            slideOutVertically { height -> -height } + fadeOut()
                                        )
                                    } else {
                                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                            slideOutVertically { height -> height } + fadeOut()
                                        )
                                    }
                                },
                                label = "counter_flip"
                            ) { count ->
                                Text(
                                    text = "$count",
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 38.sp
                                )
                            }
                            Text(
                                text = if (selectedCount == 1) "REPEAT" else "REPEATS",
                                color = SwaaraSecondaryText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Plus Button
                    StepperButton(
                        text = "+",
                        enabled = selectedCount < 99,
                        isPrimary = true,
                        onClick = {
                            if (selectedCount < 99) selectedCount++
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Preset Pills (1x, 2x, 3x, 5x, 10x)
                Text(
                    text = "QUICK PRESETS",
                    color = SwaaraSecondaryText.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    val presets = listOf(1, 2, 3, 5, 10)
                    presets.forEach { preset ->
                        val isSelected = selectedCount == preset
                        val pillScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "pill_scale_$preset"
                        )

                        Box(
                            modifier = Modifier
                                .scale(pillScale)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(SwaaraMagenta, Color(0xFFC0157C))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(SwaaraElevatedSurface, SwaaraElevatedSurface)
                                        )
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SwaaraMagenta.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCount = preset }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset}x",
                                color = if (isSelected) Color.White else SwaaraPrimaryText,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Actions: Cancel / Reset & Apply Loop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialCount > 0) {
                        // Clear Loop Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(SwaaraElevatedSurface)
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(25.dp))
                                .clickable {
                                    SwaaraLoopCounter.clear(player)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel Loop",
                                color = Color(0xFFEF4444),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Dismiss Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(SwaaraElevatedSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(25.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = SwaaraSecondaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .shadow(
                                elevation = 14.dp,
                                shape = RoundedCornerShape(25.dp),
                                spotColor = SwaaraMagenta.copy(alpha = 0.5f),
                                ambientColor = Color.Black
                            )
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SwaaraMagenta, SwaaraCyan)
                                )
                            )
                            .clickable {
                                SwaaraLoopCounter.setLoopCount(selectedCount, player)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loop Track (${selectedCount}x)",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stepper_scale"
    )

    Box(
        modifier = Modifier
            .size(54.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPrimary && enabled) Brush.linearGradient(listOf(SwaaraMagenta, Color(0xFFD800B9)))
                else Brush.linearGradient(listOf(SwaaraElevatedSurface, SwaaraElevatedSurface))
            )
            .border(
                width = 1.dp,
                color = if (isPrimary && enabled) SwaaraMagenta.copy(alpha = 0.5f)
                else Color.White.copy(alpha = 0.12f),
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isPressed = true
                onClick()
                isPressed = false
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = when {
                !enabled -> Color.White.copy(alpha = 0.25f)
                isPrimary -> Color.White
                else -> SwaaraPrimaryText
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp
        )
    }
}
