package com.varshuai.a1swaara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.R
import app.a1swaara.core.ui.LocalAppearance

@Composable
fun A1SwaraMiniPlayer(
    title: String,
    artist: String,
    thumbnailUrl: String?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = LocalAppearance.current.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1DB954))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFA14141C))
            .border(1.dp, Color(0xFF262633), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF22222E))
            ) {
                if (thumbnailUrl != null) {
                    RetryingAsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Track Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Track Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                BasicText(
                    text = title.ifEmpty { "A1 Swara Player" },
                    style = typography.xs.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                BasicText(
                    text = artist.ifEmpty { "Select a song to start" },
                    style = typography.xxs.copy(
                        color = Color(0xFFA0A0AB)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DB954))
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        colorFilter = ColorFilter.tint(Color.Black),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Next
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNextClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.play_skip_forward),
                        contentDescription = "Next",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
