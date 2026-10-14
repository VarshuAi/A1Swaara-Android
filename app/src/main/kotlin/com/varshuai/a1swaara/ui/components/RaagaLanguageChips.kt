package com.varshuai.a1swaara.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LanguageItem(
    val id: String,
    val label: String
)

val defaultLanguageItems = listOf(
    LanguageItem("for_you", "For You"),
    LanguageItem("punjabi", "Punjabi"),
    LanguageItem("hindi", "Hindi"),
    LanguageItem("english", "English"),
    LanguageItem("tamil", "Tamil"),
    LanguageItem("telugu", "Telugu"),
    LanguageItem("kannada", "Kannada"),
    LanguageItem("marathi", "Marathi"),
    LanguageItem("gujarati", "Gujarati"),
    LanguageItem("bengali", "Bengali"),
    LanguageItem("malayalam", "Malayalam")
)

@Composable
fun RaagaLanguageChips(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    languages: List<LanguageItem> = defaultLanguageItems
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        languages.forEach { lang ->
            val isSelected = selectedLanguage.equals(lang.id, ignoreCase = true) ||
                (selectedLanguage.equals("kannada", ignoreCase = true) && lang.id == "for_you" && selectedLanguage == "for_you")

            val bgModifier = if (isSelected) {
                Modifier
                    .background(SwaaraElevatedSurface)
                    .border(1.dp, SwaaraMagenta.copy(alpha = 0.50f), RoundedCornerShape(20.dp))
            } else {
                Modifier
                    .background(SwaaraDeepSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(bgModifier)
                    .clickable { onLanguageSelected(lang.id) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.label,
                    color = if (isSelected) SwaaraPrimaryText else SwaaraSecondaryText,
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
