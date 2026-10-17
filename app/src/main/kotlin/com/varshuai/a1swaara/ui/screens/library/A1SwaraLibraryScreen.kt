package com.varshuai.a1swaara.ui.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import app.a1swaara.core.ui.LocalAppearance

@Composable
fun A1SwaraLibraryScreen(
    modifier: Modifier = Modifier
) {
    val typography = LocalAppearance.current.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BasicText(
            text = "Your Library",
            style = typography.l.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Liked Songs Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1DB954))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF450AF5), Color(0xFF8E24AA), Color(0xFF1DB954))
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.heart),
                        contentDescription = "Liked",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    BasicText(
                        text = "Liked Songs",
                        style = typography.m.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    BasicText(
                        text = "Auto Playlist • A1 Swara",
                        style = typography.xs.copy(color = Color(0xFFE0E0E0))
                    )
                }
            }
        }

        BasicText(
            text = "Playlists & Albums",
            style = typography.m.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        // Sample Library Card Item
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF14141E))
                .border(1.dp, Color(0xFF222230), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1DB954))
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                BasicText(
                    text = "Favorites & Downloads",
                    style = typography.xs.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                BasicText(
                    text = "Offline Available",
                    style = typography.xxs.copy(color = Color(0xFFA0A0AB))
                )
            }
        }
    }
}
