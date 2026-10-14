package com.varshuai.a1swaara.ui.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import app.a1swaara.core.ui.LocalAppearance

data class GenreTile(val name: String, val color: Color)

@Composable
fun A1SwaraSearchScreen(
    onSearchSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = LocalAppearance.current.typography

    val genres = listOf(
        GenreTile("Pop", Color(0xFFE91E63)),
        GenreTile("Hip-Hop", Color(0xFF9C27B0)),
        GenreTile("Bollywood", Color(0xFFFF9800)),
        GenreTile("Chill", Color(0xFF00BCD4)),
        GenreTile("Rock", Color(0xFFF44336)),
        GenreTile("Electronic", Color(0xFF4CAF50)),
        GenreTile("Indie", Color(0xFF3F51B5)),
        GenreTile("Classical", Color(0xFF795548))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        BasicText(
            text = "Search & Explore",
            style = typography.l.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Input Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF161622))
                .border(1.dp, Color(0xFF282838), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.search),
                    contentDescription = "Search Icon",
                    colorFilter = ColorFilter.tint(Color(0xFF8E8E9A)),
                    modifier = Modifier.size(20.dp)
                )
                BasicText(
                    text = "Songs, Artists, Playlists...",
                    style = typography.xs.copy(color = Color(0xFF8E8E9A)),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        BasicText(
            text = "Browse Genres & Moods",
            style = typography.m.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(genres.size) { index ->
                val genre = genres[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = genre.color)
                        .clip(RoundedCornerShape(16.dp))
                        .background(genre.color)
                        .padding(14.dp)
                ) {
                    BasicText(
                        text = genre.name,
                        style = typography.s.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
