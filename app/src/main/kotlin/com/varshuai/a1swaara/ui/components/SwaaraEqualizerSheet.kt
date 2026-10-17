package com.varshuai.a1swaara.ui.components

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R

data class EqPreset(val name: String, val levels: List<Float>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwaaraEqualizerSheet(
    audioSessionId: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEqEnabled by remember { mutableStateOf(true) }
    var bassBoostLevel by remember { mutableFloatStateOf(650f) }
    var virtualizerLevel by remember { mutableFloatStateOf(400f) }

    val bandFrequencies = remember { listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz") }
    var bandLevels by remember { mutableStateOf(listOf(0f, 0f, 0f, 0f, 0f)) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    val presets = remember {
        listOf(
            EqPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
            EqPreset("Bass Heavy", listOf(6f, 4f, 1f, -1f, -2f)),
            EqPreset("Electronic", listOf(4f, 2f, -1f, 3f, 5f)),
            EqPreset("Rock", listOf(5f, 3f, -1f, 2f, 4f)),
            EqPreset("Pop", listOf(-1f, 2f, 5f, 3f, -1f)),
            EqPreset("Acoustic", listOf(3f, 2f, 2f, 3f, 2f)),
            EqPreset("Vocal Boost", listOf(-2f, -1f, 4f, 4f, 1f))
        )
    }

    // Audio effects controller
    DisposableEffect(audioSessionId) {
        if (audioSessionId <= 0) return@DisposableEffect onDispose {}

        val equalizer = runCatching { Equalizer(0, audioSessionId).apply { enabled = isEqEnabled } }.getOrNull()
        val bassBoost = runCatching { BassBoost(0, audioSessionId).apply { enabled = isEqEnabled; setStrength(bassBoostLevel.toInt().toShort()) } }.getOrNull()
        val virtualizer = runCatching { Virtualizer(0, audioSessionId).apply { enabled = isEqEnabled; setStrength(virtualizerLevel.toInt().toShort()) } }.getOrNull()

        onDispose {
            runCatching { equalizer?.release() }
            runCatching { bassBoost?.release() }
            runCatching { virtualizer?.release() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SwaaraObsidian,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.20f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                            .border(1.dp, SwaaraMagenta.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.musical_notes),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Studio Equalizer",
                            color = SwaaraPrimaryText,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = "3D Spatial Surround & Swaara Sub-Bass",
                            color = SwaaraSecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = isEqEnabled,
                    onCheckedChange = { isEqEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SwaaraMagenta,
                        uncheckedThumbColor = SwaaraSecondaryText,
                        uncheckedTrackColor = SwaaraElevatedSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Selector Chips
            Text(
                text = "ACOUSTIC PRESETS",
                color = SwaaraSecondaryText.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEachIndexed { idx, preset ->
                    val isSelected = selectedPresetIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet))
                                else Brush.horizontalGradient(listOf(SwaaraElevatedSurface, SwaaraElevatedSurface))
                            )
                            .border(
                                1.dp,
                                if (isSelected) SwaaraMagenta.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedPresetIndex = idx
                                bandLevels = preset.levels
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset.name,
                            color = if (isSelected) Color.White else SwaaraPrimaryText,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5-Band Slider Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SwaaraDeepSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PARAMETRIC FREQUENCY BANDS",
                            color = SwaaraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (isEqEnabled) "LIVE ENGINE ACTIVE" else "BYPASSED",
                            color = if (isEqEnabled) SwaaraMagenta else SwaaraSecondaryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    bandFrequencies.forEachIndexed { bandIdx, freqName ->
                        val currentDb = bandLevels.getOrElse(bandIdx) { 0f }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freqName,
                                color = SwaaraPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(65.dp)
                            )

                            Slider(
                                value = currentDb,
                                onValueChange = { newVal ->
                                    val updated = bandLevels.toMutableList()
                                    updated[bandIdx] = newVal
                                    bandLevels = updated
                                },
                                valueRange = -10f..10f,
                                enabled = isEqEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = SwaaraMagenta,
                                    activeTrackColor = SwaaraMagenta,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.10f)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${if (currentDb > 0) "+" else ""}${currentDb.toInt()} dB",
                                color = if (currentDb > 0) SwaaraCyan else SwaaraSecondaryText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bass Boost & 3D Spatial Surround Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bass Boost Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SwaaraDeepSurface)
                        .border(1.dp, SwaaraMagenta.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "BASS BOOST",
                            color = SwaaraMagenta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(bassBoostLevel / 10).toInt()}%",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Slider(
                            value = bassBoostLevel,
                            onValueChange = { bassBoostLevel = it },
                            valueRange = 0f..1000f,
                            enabled = isEqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = SwaaraMagenta,
                                activeTrackColor = SwaaraMagenta,
                                inactiveTrackColor = Color.White.copy(alpha = 0.10f)
                            )
                        )
                    }
                }

                // 3D Spatial Virtualizer Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SwaaraDeepSurface)
                        .border(1.dp, SwaaraCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "3D SPATIAL",
                            color = SwaaraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(virtualizerLevel / 10).toInt()}%",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Slider(
                            value = virtualizerLevel,
                            onValueChange = { virtualizerLevel = it },
                            valueRange = 0f..1000f,
                            enabled = isEqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = SwaaraCyan,
                                activeTrackColor = SwaaraCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.10f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
