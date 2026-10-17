package com.varshuai.a1swaara.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.varshuai.a1swaara.ui.components.SwaaraObsidian
import com.varshuai.a1swaara.ui.components.SwaaraDeepSurface
import com.varshuai.a1swaara.ui.components.SwaaraElevatedSurface
import com.varshuai.a1swaara.ui.components.SwaaraPrimaryText
import com.varshuai.a1swaara.ui.components.SwaaraSecondaryText
import com.varshuai.a1swaara.ui.components.SwaaraMagenta
import com.varshuai.a1swaara.ui.components.SwaaraViolet
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import androidx.compose.foundation.border

data class SettingCategory(
    val id: Int,
    val title: String,
    val description: String,
    val iconId: Int,
    val accentColor: Color
)

@Composable
fun SwaaraSettingsScreen(
    modifier: Modifier = Modifier
) {
    var activeSubpageId by remember { mutableIntStateOf(-1) }

    val categories = remember {
        listOf(
            SettingCategory(
                id = 0,
                title = "Appearance & Theming",
                description = "Dark, AMOLED modes, colors, fonts & roundness",
                iconId = R.drawable.color_palette,
                accentColor = SwaaraMagenta
            ),
            SettingCategory(
                id = 1,
                title = "Audio & Playback",
                description = "Streaming quality, volume, equalizer & playback",
                iconId = R.drawable.play,
                accentColor = SwaaraViolet
            ),
            SettingCategory(
                id = 2,
                title = "Cache & Storage",
                description = "Audio cache limits, storage usage & cleanup",
                iconId = R.drawable.server,
                accentColor = SwaaraCyan
            ),
            SettingCategory(
                id = 3,
                title = "Backup & Data",
                description = "Export/Import backups & listening history",
                iconId = R.drawable.server,
                accentColor = SwaaraMagenta
            ),
            SettingCategory(
                id = 4,
                title = "Accounts & Sync",
                description = "Playlist synchronization & cloud streaming",
                iconId = R.drawable.sync,
                accentColor = SwaaraViolet
            ),
            SettingCategory(
                id = 5,
                title = "Advanced & Other",
                description = "Discord RPC, Android Auto, HTTP/3 & diagnostics",
                iconId = R.drawable.shapes,
                accentColor = SwaaraCyan
            ),
            SettingCategory(
                id = 6,
                title = "About A1 Swaara",
                description = "Developed by VarshuAi (@varshuai) · v1.0.0 Official Release",
                iconId = R.drawable.information,
                accentColor = SwaaraMagenta
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
            .padding(bottom = 120.dp)
    ) {
        if (activeSubpageId == -1) {
            // Root Settings Menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Header
                Text(
                    text = "Settings",
                    color = SwaaraPrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Playback, audio, appearance & app preferences",
                    color = SwaaraSecondaryText,
                    fontSize = 12.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Category List Cards
                categories.forEach { cat ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SwaaraDeepSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable { activeSubpageId = cat.id }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color-Coded Icon Container
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(cat.accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = cat.iconId),
                                    contentDescription = cat.title,
                                    tint = cat.accentColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat.title,
                                    color = SwaaraPrimaryText,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cat.description,
                                    color = SwaaraSecondaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Subtle Navigation Chevron
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_forward),
                                contentDescription = null,
                                tint = SwaaraSecondaryText.copy(alpha = 0.45f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Sub-Settings Page Container
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Sub-header with Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activeSubpageId = -1 },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SwaaraElevatedSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_back),
                            contentDescription = "Back",
                            tint = SwaaraPrimaryText,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = categories.find { it.id == activeSubpageId }?.title ?: "Settings",
                        color = SwaaraPrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sub-Settings Screen Content
                Box(modifier = Modifier.fillMaxSize()) {
                    when (activeSubpageId) {
                        0 -> AppearanceSettings()
                        1 -> PlayerSettings()
                        2 -> CacheSettings()
                        3 -> DatabaseSettings()
                        4 -> SwaaraCloudSyncScreen()
                        5 -> OtherSettings()
                        6 -> About()
                    }
                }
            }
        }
    }
}
