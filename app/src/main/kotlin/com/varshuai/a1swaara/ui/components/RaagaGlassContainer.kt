package com.varshuai.a1swaara.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val SwaaraObsidian = Color(0xFF08080C)
val SwaaraDeepSurface = Color(0xFF111119)
val SwaaraElevatedSurface = Color(0xFF171722)
val SwaaraMagenta = Color(0xFFFF2DAA)
val SwaaraViolet = Color(0xFF8B35FF)
val SwaaraCyan = Color(0xFF20CFFF)
val SwaaraPrimaryText = Color(0xFFF7F7FA)
val SwaaraSecondaryText = Color(0xFF9696A5)

// Compatibility aliases
val RaagaPink = SwaaraMagenta
val RaagaPurple = SwaaraViolet
val RaagaCyan = SwaaraCyan
val RaagaDarkBg = SwaaraObsidian
val RaagaSurfaceElevated = SwaaraElevatedSurface

val SwaaraSignatureGradient = Brush.horizontalGradient(
    colors = listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)
)

val SwaaraSignatureGradientLinear = Brush.linearGradient(
    colors = listOf(SwaaraMagenta, SwaaraViolet, SwaaraCyan)
)

val RaagaBrandGradient = Brush.linearGradient(
    colors = listOf(SwaaraMagenta, SwaaraViolet)
)

val RaagaBrandGradientHorizontal = Brush.horizontalGradient(
    colors = listOf(SwaaraMagenta, SwaaraViolet)
)

val RaagaCyanPurpleGradient = Brush.linearGradient(
    colors = listOf(SwaaraViolet, SwaaraCyan)
)

@Composable
fun RaagaGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = SwaaraDeepSurface.copy(alpha = 0.85f),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = SwaaraViolet.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                border = BorderStroke(borderWidth, borderColor),
                shape = shape
            )
    ) {
        content()
    }
}
