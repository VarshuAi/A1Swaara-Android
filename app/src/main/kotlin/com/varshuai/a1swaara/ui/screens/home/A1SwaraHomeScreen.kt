package com.varshuai.a1swaara.ui.screens.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.ui.components.RetryingAsyncImage
import app.a1swaara.core.ui.LocalAppearance

@Composable
fun A1SwaraHomeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = LocalAppearance.current.typography
    val scrollState = rememberScrollState()
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val categories = listOf("All", "Music", "Relax", "Workout", "Focus", "Party")

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Good night"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                BasicText(
                    text = "$greeting,",
                    style = typography.xs.copy(color = Color(0xFFA0A0AB))
                )
                BasicText(
                    text = "A1 Swara Studio",
                    style = typography.l.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A24))
                        .clickable { onSearchClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Search",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A24))
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = "Settings",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(categories.size) { index ->
                val isSelected = selectedCategoryIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF1DB954) else Color(0xFF1A1A24))
                        .border(1.dp, if (isSelected) Color(0xFF1DB954) else Color(0xFF2A2A38), RoundedCornerShape(20.dp))
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    BasicText(
                        text = categories[index],
                        style = typography.xs.copy(
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        // Hero Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(180.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF1DB954))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1DB954), Color(0xFF0B5327), Color(0xFF101018))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 60.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x44FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    BasicText(
                        text = "FEATURED MIX",
                        style = typography.xxs.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Column {
                    BasicText(
                        text = "Today's Top Hits",
                        style = typography.l.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    BasicText(
                        text = "Trending tracks curated for you on A1 Swara",
                        style = typography.xs.copy(color = Color(0xFFD0D0D8))
                    )
                }
            }
        }

        // Section Title: Made for You
        BasicText(
            text = "Made For You",
            style = typography.m.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
        )

        // Carousels Cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(5) { index ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14141E))
                        .border(1.dp, Color(0xFF222230), RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF20202E))
                    )
                    BasicText(
                        text = "A1 Swara Mix ${index + 1}",
                        style = typography.xs.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    BasicText(
                        text = "Daily Mix",
                        style = typography.xxs.copy(color = Color(0xFFA0A0AB))
                    )
                }
            }
        }
    }
}
