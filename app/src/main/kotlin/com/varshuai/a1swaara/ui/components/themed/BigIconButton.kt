package com.varshuai.a1swaara.ui.components.themed

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.roundedShape

@Composable
fun BigIconButton(
    @DrawableRes iconId: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = LocalAppearance.current.colorPalette.background2,
    contentColor: Color = LocalAppearance.current.colorPalette.accent,
    shape: Shape = 32.dp.roundedShape
) = Box(
    modifier
        .shadow(12.dp, shape, spotColor = Color(0xFF1DB954))
        .clip(shape)
        .let {
            if (onClick == null) it else it.clickable(onClick = onClick)
        }
        .background(backgroundColor)
        .border(1.dp, Color(0xFF2A2A38), shape)
        .height(64.dp),
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(iconId),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        colorFilter = ColorFilter.tint(contentColor)
    )
}
