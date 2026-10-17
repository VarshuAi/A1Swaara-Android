package com.varshuai.a1swaara.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.utils.center
import com.varshuai.a1swaara.utils.disabled
import com.varshuai.a1swaara.utils.medium
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.primaryButton
import app.a1swaara.core.ui.utils.roundedShape

@Composable
fun SecondaryTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alternative: Boolean = false
) {
    val (colorPalette, typography) = LocalAppearance.current

    BasicText(
        text = text,
        style = typography.xxs.medium.center.copy(
            color = if (enabled) Color.White else colorPalette.textDisabled
        ),
        modifier = modifier
            .clip(16.dp.roundedShape)
            .clickable(enabled = enabled, onClick = onClick)
            .background(if (alternative) colorPalette.background0 else colorPalette.primaryButton)
            .border(1.dp, Color(0xFF2A2A38), 16.dp.roundedShape)
            .padding(vertical = 8.dp, horizontal = 14.dp)
    )
}
