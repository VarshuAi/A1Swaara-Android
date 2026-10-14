package com.varshuai.a1swaara.ui.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.ui.screens.LocalOpenNowPlaying
import com.varshuai.a1swaara.updater.SwaaraForceUpdateDialog
import com.varshuai.a1swaara.updater.SwaaraUpdateManager
import com.varshuai.a1swaara.utils.addNext
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.enqueue
import com.varshuai.a1swaara.utils.forcePlayAtIndex
import com.varshuai.a1swaara.utils.forcePlayFromBeginning
import com.varshuai.a1swaara.utils.thumbnail
import com.varshuai.a1swaara.utils.toast
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val SwaaraObsidian = Color(0xFF08080C)
private val SwaaraDeepSurface = Color(0xFF111119)
private val SwaaraElevatedSurface = Color(0xFF191924)
private val SwaaraMagenta = Color(0xFFFF2DAA)
private val SwaaraViolet = Color(0xFF8B35FF)
private val SwaaraCyan = Color(0xFF20CFFF)
private val SwaaraPrimaryText = Color(0xFFF1F1F5)
private val SwaaraSecondaryText = Color(0xFF8E8EA0)

@Composable
fun RaagaDownloadsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val openNowPlaying = LocalOpenNowPlaying.current
    val coroutineScope = rememberCoroutineScope()

    val isUpdateAvailable by SwaaraUpdateManager.isUpdateAvailable.collectAsStateWithLifecycle()
    val isMandatoryLockActive by SwaaraUpdateManager.isMandatoryLockActive.collectAsStateWithLifecycle()
    val updateInfo by SwaaraUpdateManager.updateInfo.collectAsStateWithLifecycle()
    val latestRelease by SwaaraUpdateManager.latestRelease.collectAsStateWithLifecycle()
    var showUpdateDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Retrieve all offline cached songs and saved tracks
    val allSongs: List<Song> by Database.songsByRowIdDesc()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val offlineSongs = remember(allSongs, binder, searchQuery, DataPreferences.downloadedSongIds) {
        allSongs.filter { song ->
            val isExplicitlyDownloaded = DataPreferences.isSongExplicitlyDownloaded(song.id)
            val isCachedOnDisk = binder?.isCached(song.id) == true
            if (!isExplicitlyDownloaded || !isCachedOnDisk) return@filter false

            if (searchQuery.isNotBlank()) {
                song.title.contains(searchQuery, ignoreCase = true) ||
                    (song.artistsText?.contains(searchQuery, ignoreCase = true) == true)
            } else true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
            .statusBarsPadding()
            .padding(bottom = 120.dp)
    ) {
        // Mandatory Update Warning Banner if online streaming is locked
        if (isUpdateAvailable && isMandatoryLockActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SwaaraElevatedSurface)
                    .border(1.5.dp, SwaaraMagenta.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable { showUpdateDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SwaaraMagenta.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.download),
                                contentDescription = null,
                                tint = SwaaraMagenta,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Offline Mode Active • Update Required",
                                color = SwaaraPrimaryText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Online streaming locked. Only offline tracks playable.",
                                color = SwaaraSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Update",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        // 1. Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Downloads",
                    color = SwaaraPrimaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (offlineSongs.isNotEmpty()) "${offlineSongs.size} Songs • Ready Offline (320 kbps)"
                           else "Saved offline music library",
                    color = SwaaraSecondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Search Toggle Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SwaaraElevatedSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                    .clickable { isSearchActive = !isSearchActive },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search Downloads",
                    tint = if (isSearchActive) SwaaraCyan else SwaaraPrimaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. Search Bar (Animated Expandable)
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SwaaraDeepSurface)
                    .border(1.dp, SwaaraCyan.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null,
                        tint = SwaaraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = SwaaraPrimaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(SwaaraCyan),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search downloaded tracks...",
                                    color = SwaaraSecondaryText.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Clear",
                            tint = SwaaraSecondaryText,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }
        }

        // 3. Action Buttons Row: [ Play All ] and [ Shuffle ]
        if (offlineSongs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play All Button (Gradient Primary Hero)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = SwaaraMagenta.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                        .clickable {
                            openNowPlaying()
                            binder?.player?.forcePlayFromBeginning(offlineSongs.map { it.asMediaItem })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Play All",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Shuffle Button (Glass Elevated Secondary)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .clickable {
                            openNowPlaying()
                            binder?.player?.forcePlayFromBeginning(offlineSongs.shuffled().map { it.asMediaItem })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.shuffle),
                            contentDescription = null,
                            tint = SwaaraCyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "Shuffle",
                            color = SwaaraPrimaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Offline Songs List
        if (offlineSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SwaaraDeepSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.download),
                            contentDescription = null,
                            tint = SwaaraMagenta.copy(alpha = 0.70f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching downloads" else "No offline downloads yet",
                        color = SwaaraPrimaryText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Try searching for a different song or artist"
                               else "Tap the download button on any song to save it offline in HQ 320 kbps.",
                        color = SwaaraSecondaryText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(offlineSongs, key = { _, song -> song.id }) { index, song: Song ->
                    var showMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SwaaraDeepSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable {
                                openNowPlaying()
                                binder?.player?.forcePlayAtIndex(
                                    offlineSongs.map { it.asMediaItem },
                                    index
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork Thumbnail
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SwaaraElevatedSurface)
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl?.thumbnail(800)
                                    ?: "https://i.ytimg.com/vi/${song.id}/hqdefault.jpg",
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Artist
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = SwaaraPrimaryText,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Offline Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SwaaraCyan.copy(alpha = 0.14f))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "HQ",
                                        color = SwaaraCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Text(
                                    text = song.artistsText ?: "Offline Ready",
                                    color = SwaaraSecondaryText,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Duration or Action Menu
                        song.durationText?.let { duration ->
                            Text(
                                text = duration,
                                color = SwaaraSecondaryText.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        // Options Menu Button
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ellipsis_horizontal),
                                    contentDescription = "Options",
                                    tint = SwaaraSecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(SwaaraElevatedSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Play Next", color = SwaaraPrimaryText) },
                                    onClick = {
                                        showMenu = false
                                        binder?.player?.addNext(song.asMediaItem)
                                        context.toast("Playing \"${song.title}\" next")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.play_skip_forward),
                                            contentDescription = null,
                                            tint = SwaaraCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add to Queue", color = SwaaraPrimaryText) },
                                    onClick = {
                                        showMenu = false
                                        binder?.player?.enqueue(song.asMediaItem)
                                        context.toast("Added to queue")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.playlist),
                                            contentDescription = null,
                                            tint = SwaaraMagenta,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove from Downloads", color = Color(0xFFFF5252)) },
                                    onClick = {
                                        showMenu = false
                                        DataPreferences.removeDownloadedSongId(song.id)
                                        binder?.removeDownloadedSong(song.id)
                                        coroutineScope.launch(Dispatchers.IO) {
                                            Database.delete(song)
                                        }
                                        context.toast("Removed \"${song.title}\" from downloads")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.trash),
                                            contentDescription = null,
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpdateDialog && isUpdateAvailable && (updateInfo != null || latestRelease != null)) {
        SwaaraForceUpdateDialog(
            release = latestRelease,
            updateInfo = updateInfo,
            onDismiss = { showUpdateDialog = false }
        )
    }
}
