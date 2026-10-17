package com.varshuai.a1swaara.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.varshuai.a1swaara.utils.AmbientColors
import com.varshuai.a1swaara.utils.FastBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Universal ambient background that guarantees a smooth, gorgeous blur and atmospheric
 * lighting across ALL Android versions (API 23 to API 36+).
 *
 * Tier 1: Dynamic mesh glow Canvas with dual radial gradient orbs (works on 100% of devices).
 * Tier 2: CPU Fast StackBlur on downscaled bitmap for silky smooth diffusion on Android 11 and below.
 * Tier 3: Hardware RenderEffect blur layer on Android 12+ (API 31+).
 * Tier 4: Dark obsidian scrim and vignette for optimal text readability and contrast.
 */
@Composable
fun SwaaraAmbientBackground(
    artworkUri: Any?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val context = LocalContext.current
    var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var ambientColors by remember {
        mutableStateOf(
            AmbientColors(
                dominant = SwaaraViolet,
                vibrant = SwaaraMagenta,
                darkVibrant = SwaaraDeepSurface
            )
        )
    }

    LaunchedEffect(artworkUri) {
        if (artworkUri == null || (artworkUri is String && artworkUri.isBlank())) {
            blurredBitmap = null
            ambientColors = AmbientColors(
                dominant = SwaaraViolet,
                vibrant = SwaaraMagenta,
                darkVibrant = SwaaraDeepSurface
            )
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val rawBitmap = result.image.toBitmap()
                    // Downscale to 48x48 for ultra-fast, smooth blur
                    val scaled = Bitmap.createScaledBitmap(rawBitmap, 48, 48, true)
                    val colors = FastBlur.extractAmbientColors(scaled)
                    val blurred = FastBlur.stackBlur(scaled, radius = 10, canReuse = true)
                    withContext(Dispatchers.Main) {
                        ambientColors = colors
                        blurredBitmap = blurred
                    }
                }
            } catch (_: Throwable) {
                // Keep default ambient colors on error
            }
        }
    }

    val animatedVibrant by animateColorAsState(
        targetValue = ambientColors.vibrant,
        animationSpec = tween(durationMillis = 700),
        label = "ambient_vibrant"
    )
    val animatedDominant by animateColorAsState(
        targetValue = ambientColors.dominant,
        animationSpec = tween(durationMillis = 700),
        label = "ambient_dominant"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
    ) {
        // Tier 1: Universal Mesh Glow (Works on all Android versions)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Top-center vibrant radiant orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedVibrant.copy(alpha = 0.42f),
                        animatedVibrant.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.35f, canvasH * 0.28f),
                    radius = canvasW * 0.95f
                ),
                center = Offset(canvasW * 0.35f, canvasH * 0.28f),
                radius = canvasW * 0.95f
            )

            // Center-bottom dominant radiant orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedDominant.copy(alpha = 0.38f),
                        animatedDominant.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.70f, canvasH * 0.65f),
                    radius = canvasW * 0.90f
                ),
                center = Offset(canvasW * 0.70f, canvasH * 0.65f),
                radius = canvasW * 0.90f
            )
        }

        // Tier 2 & 3: Blurred Bitmap Artwork (CPU StackBlur + optional API 31+ hardware blur)
        blurredBitmap?.let { bmp ->
            val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier
                    .fillMaxSize()
                    .blur(28.dp)
            } else {
                Modifier.fillMaxSize()
            }

            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = blurModifier.alpha(0.55f)
            )
        }

        // Tier 4: Dark obsidian gradient scrim to guarantee crisp foreground contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SwaaraObsidian.copy(alpha = 0.48f),
                            SwaaraObsidian.copy(alpha = 0.68f),
                            SwaaraObsidian.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // Foreground content
        content()
    }
}
