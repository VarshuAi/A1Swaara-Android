package com.varshuai.a1swaara.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Album
import com.varshuai.a1swaara.models.Artist
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.PlaylistPreview
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute
import com.varshuai.a1swaara.ui.screens.localPlaylistRoute
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import com.varshuai.a1swaara.ui.components.SwaaraDisplayItem
import com.varshuai.a1swaara.ui.components.SwaaraGlassContainer
import com.varshuai.a1swaara.ui.components.SwaaraPink
import com.varshuai.a1swaara.ui.components.SwaaraPurple
import com.varshuai.a1swaara.ui.components.SwaaraTrendingShelf
import com.varshuai.a1swaara.ui.screens.LocalOpenNowPlaying
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.playTrack
import com.varshuai.a1swaara.utils.thumbnail
import app.a1swaara.core.data.enums.ArtistSortBy
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder
import app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.utils.intent
import com.varshuai.a1swaara.cloud.SwaaraCloudSync
import com.varshuai.a1swaara.ui.components.SwaaraCloudSyncDialog
import com.varshuai.a1swaara.ui.components.SwaaraJamRoomDialog
import com.varshuai.a1swaara.ui.components.SwaaraSharePlaylistDialog
import com.varshuai.a1swaara.cloud.SwaaraSharedPlaylists
import com.varshuai.a1swaara.utils.toast

@Composable
fun SwaaraLibraryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val binder = LocalPlayerServiceBinder.current
    val openNowPlaying = LocalOpenNowPlaying.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember { listOf("Playlists", "Songs", "Albums", "Artists", "Heavy Rotation", "History") }

    val likedSongs by Database.favorites()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val allSongs by Database.songsByRowIdDesc()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val savedAlbums by Database.albumsByRowIdDesc()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val heavyRotationSongs by Database.songsByPlayTimeDesc()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val historySongs by Database.history()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val userPlaylists by Database.playlistPreviews(PlaylistSortBy.DateAdded, SortOrder.Descending)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val artistsList by Database.artists(ArtistSortBy.DateAdded, SortOrder.Descending)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var recommendedSongs by remember { mutableStateOf<List<SwaaraDisplayItem>>(emptyList()) }

    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showJamDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    if (showCloudSyncDialog) {
        SwaaraCloudSyncDialog(onDismiss = { showCloudSyncDialog = false })
    }
    if (showJamDialog) {
        SwaaraJamRoomDialog(onDismiss = { showJamDialog = false })
    }
    if (showImportDialog) {
        SwaaraSharePlaylistDialog(onDismiss = { showImportDialog = false })
    }

    // Load trending library recommendations
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = container.searchResultRepository.searchSongs("top chartbusters", null)?.getOrNull()?.items
            if (!result.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    recommendedSongs = result.take(10).map { item ->
                        SwaaraDisplayItem(
                            id = item.key,
                            title = item.info?.name ?: "Top Track",
                            subtitle = item.authors?.joinToString { it.name.orEmpty() } ?: "Artist",
                            thumbnailUrl = item.thumbnail?.url?.thumbnail(800) ?: "https://i.ytimg.com/vi/${item.key}/maxresdefault.jpg"
                        )
                    }
                }
            }
        }
    }

    // Play helper with infinite auto radio
    val playSong: (Song) -> Unit = { song ->
        openNowPlaying()
        if (binder != null) {
            binder.playTrack(
                videoId = song.id,
                title = song.title,
                artist = song.artistsText,
                thumbnailUrl = song.thumbnailUrl,
                durationText = song.durationText
            )
        } else {
            try {
                context.startService(context.intent<PlayerService>())
            } catch (_: Exception) {}
            coroutineScope.launch {
                val activeBinder = snapshotFlow { binder }.filterNotNull().first()
                activeBinder.playTrack(
                    videoId = song.id,
                    title = song.title,
                    artist = song.artistsText,
                    thumbnailUrl = song.thumbnailUrl,
                    durationText = song.durationText
                )
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = Color(0xFF141424),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "New Playlist",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("My Awesome Playlist", color = Color.White.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SwaaraPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1B1B2C),
                        unfocusedContainerColor = Color(0xFF1B1B2C)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                Database.insert(Playlist(name = newPlaylistName.trim()))
                            }
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwaaraPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07070B))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 130.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Library",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${allSongs.size} tracks • ${userPlaylists.size} playlists • ${savedAlbums.size} albums",
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 13.sp
                )
            }

            // Action Buttons: Jam Room + Cloud Sync + Create Playlist
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Jam Room Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1B2C))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { showJamDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.radio),
                        contentDescription = "Live Jam",
                        tint = SwaaraPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Cloud Sync Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1B2C))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { showCloudSyncDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sync),
                        contentDescription = "Cloud Sync",
                        tint = SwaaraCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Create Playlist Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SwaaraPink, SwaaraPurple)))
                        .clickable { showCreatePlaylistDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "New Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Tab Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(listOf(SwaaraPink, SwaaraPurple))
                            else Brush.horizontalGradient(listOf(Color(0xFF151524), Color(0xFF151524)))
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tab,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Liked Songs Gradient Card
        if (selectedTabIndex == 0 || selectedTabIndex == 1) {
            SwaaraGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable {
                        likedSongs.firstOrNull()?.let { playSong(it) }
                    },
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFF1F1235).copy(alpha = 0.88f),
                borderColor = SwaaraPink.copy(alpha = 0.40f),
                borderWidth = 1.2.dp,
                shadowElevation = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = SwaaraPink)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(SwaaraPink, SwaaraPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.heart),
                            contentDescription = "Liked Songs",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liked Songs",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${likedSongs.size} favorite tracks",
                            color = SwaaraCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Play Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SwaaraPink, SwaaraPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Playlists Tab: User Playlists + Liked + Heavy Rotation
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Playlists",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Import Shared Playlist Quick Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B1B2C))
                                .border(1.dp, SwaaraCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { showImportDialog = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.download),
                                    contentDescription = null,
                                    tint = SwaaraCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Import Code",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SwaaraCyan
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (userPlaylists.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131322))
                                .clickable { showCreatePlaylistDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add),
                                    contentDescription = null,
                                    tint = SwaaraPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Create your first playlist",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap to organize your favorite tracks",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        userPlaylists.forEach { playlist: PlaylistPreview ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF131322))
                                    .clickable {
                                        localPlaylistRoute.global(playlist.playlist.id)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF880E4F)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.playlist),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.playlist.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${playlist.songCount} songs",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }

                                // Quick Share Playlist Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .clickable {
                                            coroutineScope.launch {
                                                val res = SwaaraSharedPlaylists.publishPlaylist(context, playlist.playlist.id)
                                                if (res.success && res.code != null) {
                                                    val sendIntent = android.content.Intent().apply {
                                                        action = android.content.Intent.ACTION_SEND
                                                        putExtra(
                                                            android.content.Intent.EXTRA_TEXT,
                                                            "Check out my playlist '${playlist.playlist.name}' on A1 Swaara! Use share code: ${res.code}"
                                                        )
                                                        type = "text/plain"
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Playlist Code"))
                                                } else {
                                                    context.toast(res.message)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.share_social),
                                        contentDescription = "Share Playlist",
                                        tint = SwaaraCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            }

            1 -> {
                // Songs Tab: All Songs
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        text = "All Saved Songs (${allSongs.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    allSongs.take(30).forEach { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF131322))
                                .clickable { playSong(song) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl?.thumbnail(800) ?: "https://i.ytimg.com/vi/${song.id}/maxresdefault.jpg",
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artistsText ?: "Artist",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Albums Tab: Saved / Bookmarked Albums
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        text = "Saved Albums (${savedAlbums.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (savedAlbums.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.disc),
                                    contentDescription = null,
                                    tint = SwaaraPurple,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Saved Albums Yet",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the bookmark icon on any album to save it here",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        savedAlbums.forEach { album: Album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF131322))
                                    .clickable {
                                        albumRoute.global(album.id)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.DarkGray)
                                ) {
                                    AsyncImage(
                                        model = album.thumbnailUrl?.thumbnail(800),
                                        contentDescription = album.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = album.title ?: "Album",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = listOfNotNull(album.authorsText, album.year).joinToString(" • ").ifBlank { "Album" },
                                        color = Color.White.copy(alpha = 0.55f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    painter = painterResource(R.drawable.chevron_forward),
                                    contentDescription = "View Album",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // Artists Tab
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        text = "Followed Artists (${artistsList.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (artistsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = null,
                                    tint = SwaaraPurple,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Followed Artists Yet",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the bookmark icon on any artist profile to follow them",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        artistsList.forEach { artist: Artist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF131322))
                                    .clickable {
                                        artistRoute.global(artist.id)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(SwaaraPink, SwaaraPurple))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artist.name?.take(1) ?: "A",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AsyncImage(
                                        model = artist.thumbnailUrl?.thumbnail(800),
                                        contentDescription = artist.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artist.name ?: "Artist",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Artist Profile",
                                        color = SwaaraCyan,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(
                                    painter = painterResource(R.drawable.chevron_forward),
                                    contentDescription = "View Artist",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                // Heavy Rotation (Most Played Songs)
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        text = "Heavy Rotation (Most Played)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    heavyRotationSongs.take(25).forEachIndexed { idx, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF131322))
                                .clickable { playSong(song) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${idx + 1}",
                                color = if (idx < 3) SwaaraPink else Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl?.thumbnail(800) ?: "https://i.ytimg.com/vi/${song.id}/maxresdefault.jpg",
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artistsText ?: "Artist",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            5 -> {
                // History Tab
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Text(
                        text = "Recently Played",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    historySongs.take(25).forEach { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF131322))
                                .clickable { playSong(song) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl?.thumbnail(800) ?: "https://i.ytimg.com/vi/${song.id}/maxresdefault.jpg",
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artistsText ?: "Artist",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Recommendations Section (Always fills Library with fresh music)
        if (recommendedSongs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SwaaraTrendingShelf(
                title = "Recommended For Your Library",
                subtitle = "Discover and save tracks to your collection",
                items = recommendedSongs,
                onItemClick = { item ->
                    openNowPlaying()
                    if (binder != null) {
                        binder.playTrack(
                            videoId = item.id,
                            title = item.title,
                            artist = item.subtitle,
                            thumbnailUrl = item.thumbnailUrl
                        )
                    } else {
                        try {
                            context.startService(context.intent<PlayerService>())
                        } catch (_: Exception) {}
                        coroutineScope.launch {
                            val activeBinder = snapshotFlow { binder }.filterNotNull().first()
                            activeBinder.playTrack(
                                videoId = item.id,
                                title = item.title,
                                artist = item.subtitle,
                                thumbnailUrl = item.thumbnailUrl
                            )
                        }
                    }
                }
            )
        }
    }
}
