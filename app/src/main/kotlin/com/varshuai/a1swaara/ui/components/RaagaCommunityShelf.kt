package com.varshuai.a1swaara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.cloud.TrendingSong

@Composable
fun RaagaCommunityShelf(
    songs: List<TrendingSong>,
    onSongClick: (TrendingSong) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "COMMUNITY CHARTS",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Leaderboard",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwaaraPrimaryText
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Most played songs across all A1 Swaara listeners right now",
                    fontSize = 12.sp,
                    color = SwaaraSecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(songs) { song ->
                CommunitySongCard(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
private fun CommunitySongCard(
    song: TrendingSong,
    onClick: () -> Unit
) {
    val playsFormatted = when {
        song.playCount >= 1000 -> "${song.playCount / 1000}K plays"
        else -> "${song.playCount} plays"
    }

    Column(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SwaaraElevatedSurface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        // Artwork Box with Rank & Play Badge
        Box(
            modifier = Modifier
                .size(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141424))
        ) {
            AsyncImage(
                model = song.artworkUrl?.takeIf { it.isNotBlank() }
                    ?: "https://i.ytimg.com/vi/${song.songId}/hqdefault.jpg",
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Rank Pill (Top-Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, SwaaraCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "#${song.rank}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = SwaaraCyan
                )
            }

            // Play count badge (Bottom-End)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = playsFormatted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = song.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SwaaraPrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Artist
        Text(
            text = song.artist,
            fontSize = 11.sp,
            color = SwaaraSecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
