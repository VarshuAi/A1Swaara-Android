package com.varshuai.a1swaara.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshuai.a1swaara.R
import coil3.compose.AsyncImage

@Composable
fun SwaaraStoryCardDialog(
    songTitle: String,
    artistName: String,
    artworkUri: Uri?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val shareStoryText = "Listening to $songTitle by $artistName on A1 Swaara"

    val shareIntent = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, songTitle)
            putExtra(Intent.EXTRA_TEXT, shareStoryText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Song"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF10101E),
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.share_social),
                    contentDescription = null,
                    tint = SwaaraPink,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Share Story Card",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Story Sticker Card Preview
                SwaaraGlassContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFF171328).copy(alpha = 0.95f),
                    borderColor = SwaaraPink.copy(alpha = 0.45f),
                    borderWidth = 1.2.dp,
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Artwork with Neon Glow
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = SwaaraPink, spotColor = SwaaraPurple)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = artworkUri,
                                contentDescription = songTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Waveform Graphic
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(24.dp)
                        ) {
                            val bars = listOf(8, 14, 22, 12, 18, 24, 10, 16, 20, 14, 8, 18, 22, 12)
                            bars.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(h.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Brush.verticalGradient(listOf(SwaaraPink, SwaaraCyan)))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = songTitle,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = artistName,
                            color = SwaaraCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // App Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.musical_notes),
                                contentDescription = null,
                                tint = SwaaraPink,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "A1 SWAARA",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    shareIntent()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SwaaraPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Share Card", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
