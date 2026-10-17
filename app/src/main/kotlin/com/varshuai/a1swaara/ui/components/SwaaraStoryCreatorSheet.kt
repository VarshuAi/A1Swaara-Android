package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.utils.SwaaraStoryExporter
import com.varshuai.a1swaara.utils.thumbnail
import com.varshuai.a1swaara.utils.toast
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwaaraStoryCreatorSheet(
    title: String,
    artist: String,
    artworkUrl: String?,
    songDurationMs: Long,
    player: Player?,
    videoId: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Image Story, 1 = Video Story
    var selectedVibe by remember { mutableStateOf("Listening") }
    var videoDurationSeconds by remember { mutableIntStateOf(15) } // 15s or 30s
    var snippetStartSec by remember { mutableFloatStateOf(0f) }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableIntStateOf(0) }
    var exportStatusText by remember { mutableStateOf("") }

    val totalDurationSec = (songDurationMs / 1000f).coerceAtLeast(30f)
    val maxStartSec = (totalDurationSec - videoDurationSeconds).coerceAtLeast(0f)

    // Animation for dynamic dancing notes
    val infiniteTransition = rememberInfiniteTransition(label = "story")
    val noteFloatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "notes"
    )
    val eqPulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq"
    )

    // Audio preview snippet scrub
    LaunchedEffect(isPreviewPlaying, snippetStartSec) {
        if (isPreviewPlaying && player != null) {
            player.seekTo((snippetStartSec * 1000).toLong())
            player.play()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isPreviewPlaying && player != null) {
                player.pause()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SwaaraObsidian,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Title & Done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Story Creator Studio",
                    color = SwaaraPrimaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Done",
                    color = SwaaraCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 2. Mode Selector: [ Image Story | Video Story (30s/60s) ]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(SwaaraDeepSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                    .padding(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Image Story
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedMode == 0) Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet))
                                else SolidColor(Color.Transparent)
                            )
                            .clickable { selectedMode = 0 }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.disc),
                                contentDescription = null,
                                tint = if (selectedMode == 0) Color.White else SwaaraSecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Image Story",
                                color = if (selectedMode == 0) Color.White else SwaaraSecondaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Video Story
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selectedMode == 1) Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet))
                                else SolidColor(Color.Transparent)
                            )
                            .clickable { selectedMode = 1 }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.film),
                                contentDescription = null,
                                tint = if (selectedMode == 1) Color.White else SwaaraSecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Video Story",
                                color = if (selectedMode == 1) Color.White else SwaaraSecondaryText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Swaara Music Trimmer Studio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SwaaraDeepSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Album Artwork & Track Info
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = SwaaraMagenta.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, SwaaraMagenta.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = artworkUrl?.thumbnail(800) ?: "https://i.ytimg.com/vi/placeholder/hqdefault.jpg",
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    color = SwaaraPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    color = SwaaraSecondaryText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timeline Controls Row: ( 30s/60s ) on left | Progress dots | [ Play/Stop ] on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Duration Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SwaaraElevatedSurface)
                            .border(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)),
                                CircleShape
                            )
                            .clickable {
                                videoDurationSeconds = if (videoDurationSeconds == 15) 30 else 15
                                if (snippetStartSec > (totalDurationSec - videoDurationSeconds)) {
                                    snippetStartSec = (totalDurationSec - videoDurationSeconds).coerceAtLeast(0f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$videoDurationSeconds",
                            color = SwaaraPrimaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Snippet Time Indicators
                    val startInt = snippetStartSec.toInt()
                    val endInt = (snippetStartSec + videoDurationSeconds).toInt().coerceAtMost(totalDurationSec.toInt())
                    Text(
                        text = "${formatSec(startInt)} - ${formatSec(endInt)}",
                        color = SwaaraPrimaryText.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Play / Stop Preview Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPreviewPlaying) SwaaraMagenta
                                else Color.White
                            )
                            .clickable { isPreviewPlaying = !isPreviewPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPreviewPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = "Preview",
                            tint = if (isPreviewPlaying) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Swaara Waveform Scrubber with Yellow/Pink Highlight Window
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0C0C12))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val fullWidthPx = constraints.maxWidth.toFloat()
                    val windowRatio = (videoDurationSeconds / totalDurationSec).coerceIn(0.2f, 0.6f)
                    val windowWidthPx = fullWidthPx * windowRatio
                    val maxOffsetPx = fullWidthPx - windowWidthPx
                    val currentProgressRatio = (snippetStartSec / maxStartSec.coerceAtLeast(0.1f)).coerceIn(0f, 1f)
                    val currentOffsetPx = currentProgressRatio * maxOffsetPx

                    // Background Audio Waveform Equalizer Ticks
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tickCount = 32
                        for (i in 0 until tickCount) {
                            val h = when (i % 5) {
                                0 -> 16.dp
                                1 -> 28.dp
                                2 -> 20.dp
                                3 -> 34.dp
                                else -> 22.dp
                            }
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color.White.copy(alpha = 0.20f))
                            )
                        }
                    }

                    // Sliding Highlight Window (Draggable capsule)
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(currentOffsetPx.roundToInt(), 0) }
                            .width((windowWidthPx / (LocalContext.current.resources.displayMetrics.density)).dp)
                            .fillMaxHeight()
                            .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFFFF9800))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(
                                2.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF2DAA))),
                                RoundedCornerShape(10.dp)
                            )
                            .pointerInput(maxStartSec) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newOffset = (currentOffsetPx + dragAmount.x).coerceIn(0f, maxOffsetPx)
                                    val newRatio = if (maxOffsetPx > 0) newOffset / maxOffsetPx else 0f
                                    snippetStartSec = (newRatio * maxStartSec).coerceIn(0f, maxStartSec)
                                }
                            }
                    )
                }

                // Smooth Linear Slider Fine-tuner
                Slider(
                    value = snippetStartSec,
                    onValueChange = { snippetStartSec = it },
                    valueRange = 0f..maxStartSec.coerceAtLeast(0.1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SwaaraMagenta,
                        activeTrackColor = SwaaraMagenta,
                        inactiveTrackColor = Color.White.copy(alpha = 0.10f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. ACTION BUTTONS: [ Save to Local ] and [ Share Story ]
        if (isExporting) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SwaaraDeepSurface)
                    .border(1.dp, SwaaraMagenta.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SwaaraMagenta,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (exportStatusText.isNotEmpty()) exportStatusText else "Rendering Story...",
                            color = SwaaraPrimaryText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$exportProgress%",
                        color = SwaaraCyan,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (exportProgress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(3.5.dp)),
                    color = SwaaraMagenta,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Creating ultra-smooth HD music story...",
                    color = SwaaraSecondaryText,
                    fontSize = 11.5.sp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save to Local Button (Gallery)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(25.dp))
                        .clickable {
                            isExporting = true
                            exportProgress = 5
                            exportStatusText = if (selectedMode == 0) "Saving Image..." else "Exporting Video..."
                            coroutineScope.launch {
                                try {
                                    if (selectedMode == 0) {
                                        SwaaraStoryExporter.saveImageToGallery(context, title, artist, artworkUrl)
                                    } else {
                                        SwaaraStoryExporter.saveVideoToGallery(
                                            context = context,
                                            title = title,
                                            artist = artist,
                                            artworkUrl = artworkUrl,
                                            durationSeconds = videoDurationSeconds,
                                            snippetStartSec = snippetStartSec,
                                            videoId = videoId,
                                            onProgress = { p ->
                                                exportProgress = p
                                                exportStatusText = "Exporting: $p%"
                                            }
                                        )
                                    }
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.download),
                            contentDescription = null,
                            tint = SwaaraPrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (selectedMode == 0) "Save Image" else "Save Video",
                            color = SwaaraPrimaryText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Share Story Button (Social / WhatsApp)
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(25.dp),
                            ambientColor = Color.Black.copy(alpha = 0.6f),
                            spotColor = SwaaraMagenta.copy(alpha = 0.45f)
                        )
                        .clip(RoundedCornerShape(25.dp))
                        .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                        .clickable {
                            isExporting = true
                            exportProgress = 5
                            exportStatusText = if (selectedMode == 0) "Preparing Image..." else "Generating Video..."
                            coroutineScope.launch {
                                try {
                                    if (selectedMode == 0) {
                                        SwaaraStoryExporter.shareImageStory(context, title, artist, artworkUrl, videoId)
                                        onDismiss()
                                    } else {
                                        SwaaraStoryExporter.shareVideoStory(
                                            context = context,
                                            title = title,
                                            artist = artist,
                                            artworkUrl = artworkUrl,
                                            durationSeconds = videoDurationSeconds,
                                            snippetStartSec = snippetStartSec,
                                            videoId = videoId,
                                            onProgress = { p ->
                                                exportProgress = p
                                                exportStatusText = "Encoding: $p%"
                                            }
                                        )
                                        onDismiss()
                                    }
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.share_social),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (selectedMode == 0) "Share Image Story" else "Share Video Story",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatSec(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
