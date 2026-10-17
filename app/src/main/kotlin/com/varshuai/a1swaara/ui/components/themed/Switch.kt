package com.varshuai.a1swaara.ui.components.themed

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.ui.components.SwaaraPink
import com.varshuai.a1swaara.ui.components.SwaaraPurple
import com.varshuai.a1swaara.utils.drawCircle

@Composable
fun Switch(
    isChecked: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isChecked, label = null)

    val backgroundColor by transition.animateColor(label = "") {
        if (it) SwaaraPurple else Color(0xFF232336)
    }

    val color by transition.animateColor(label = "") {
        if (it) Color.White else Color(0xFF75758C)
    }

    val offset by transition.animateDp(label = "") {
        if (it) 36.dp else 12.dp
    }

    Canvas(modifier = modifier.size(width = 48.dp, height = 24.dp)) {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx())
        )

        drawCircle(
            color = color,
            radius = 8.dp.toPx(),
            center = size.center.copy(x = offset.toPx()),
            shadow = Shadow(
                color = if (isChecked) SwaaraPink.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f),
                blurRadius = 8.dp.toPx(),
                offset = Offset(x = 0f, y = 1.dp.toPx())
            )
        )
    }
}
