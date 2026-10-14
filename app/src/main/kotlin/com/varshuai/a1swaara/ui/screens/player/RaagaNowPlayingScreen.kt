package com.varshuai.a1swaara.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import com.varshuai.a1swaara.ui.components.SwaaraAmbientBackground
import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.varshuai.a1swaara.downloader.SwaaraFastDownloader
import com.varshuai.a1swaara.ui.components.SwaaraStoryCreatorSheet
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.SongPlaylistMap
import com.varshuai.a1swaara.ui.components.themed.TextFieldDialog

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.preferences.PlayerPreferences
import com.varshuai.a1swaara.ui.components.RaagaEqualizerSheet
import com.varshuai.a1swaara.ui.components.RaagaFuturisticScrubBar
import com.varshuai.a1swaara.ui.components.RaagaGlassContainer
import com.varshuai.a1swaara.ui.components.RaagaPulsingPlayButton
import com.varshuai.a1swaara.ui.components.RaagaSleepTimerDialog
import com.varshuai.a1swaara.ui.components.SwaaraBoomAudioEngine
import com.varshuai.a1swaara.ui.components.SwaaraBoomAura
import com.varshuai.a1swaara.ui.components.SwaaraBoomSpectrum
import com.varshuai.a1swaara.ui.components.SwaaraBoomButton
import com.varshuai.a1swaara.ui.components.SwaaraObsidian
import com.varshuai.a1swaara.ui.components.SwaaraDeepSurface
import com.varshuai.a1swaara.ui.components.SwaaraElevatedSurface
import com.varshuai.a1swaara.ui.components.SwaaraPrimaryText
import com.varshuai.a1swaara.ui.components.SwaaraSecondaryText
import com.varshuai.a1swaara.ui.components.SwaaraMagenta
import com.varshuai.a1swaara.ui.components.SwaaraViolet
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import com.varshuai.a1swaara.cloud.SwaaraCommunityCharts
import com.varshuai.a1swaara.cloud.SwaaraListenTogether
import com.varshuai.a1swaara.ui.components.SwaaraJamRoomDialog
import com.varshuai.a1swaara.utils.playTrack
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.varshuai.a1swaara.service.PrecacheService
import com.varshuai.a1swaara.utils.toast
import com.varshuai.a1swaara.utils.forceSeekToNext
import com.varshuai.a1swaara.utils.forceSeekToPrevious
import com.varshuai.a1swaara.utils.shouldBePlaying
import com.varshuai.a1swaara.utils.thumbnail
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaagaNowPlayingScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player

    if (player == null || binder == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF07070B)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = SwaaraMagenta,
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val artworkSize = if (isLandscape) 180.dp else minOf(315.dp, (configuration.screenWidthDp - 64).dp)
    val auraSize = artworkSize + 20.dp

    var currentMediaItem by remember { mutableStateOf(player.currentMediaItem) }
    var isPlaying by remember { mutableStateOf(player.shouldBePlaying) }
    var isLoading by remember { mutableStateOf(player.playbackState == Player.STATE_BUFFERING) }
    var currentPosition by remember { mutableLongStateOf(player.currentPosition) }
    var duration by remember { mutableLongStateOf(player.duration.coerceAtLeast(1L)) }

    // Real-time track transition listener
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaItem = mediaItem
            }
            override fun onEvents(p: Player, events: Player.Events) {
                currentMediaItem = p.currentMediaItem
                isPlaying = p.shouldBePlaying
                isLoading = p.playbackState == Player.STATE_BUFFERING
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(player) {
        while (true) {
            currentMediaItem = player.currentMediaItem
            isPlaying = player.shouldBePlaying
            isLoading = player.playbackState == Player.STATE_BUFFERING
            currentPosition = player.currentPosition
            duration = player.duration.coerceAtLeast(1L)
            delay(250)
        }
    }

    val metadata = currentMediaItem?.mediaMetadata
    val videoId = currentMediaItem?.mediaId
    val title = metadata?.title?.toString() ?: "No Song Playing"
    val rawArtist = metadata?.artist?.toString() ?: "Unknown Artist"
    val artist = rawArtist.replace(Regex("[,&\\s]{2,}"), ", ").trim(',', ' ')
    val artworkUri = metadata?.artworkUri?.toString()?.thumbnail(1200)
        ?: if (!videoId.isNullOrBlank()) "https://i.ytimg.com/vi/$videoId/hq720.jpg" else null

    // Room DB Liked Status Flow
    val likedAt by remember(videoId) {
        if (videoId != null) Database.likedAt(videoId) else flowOf(null)
    }.collectAsState(initial = null)
    val isLiked = likedAt != null

    // Sleep Timer Status
    val sleepTimerMillisLeft = binder.sleepTimerMillisLeft?.collectAsState(initial = null)?.value
    val isSleepTimerActive = sleepTimerMillisLeft != null && sleepTimerMillisLeft > 0

    var isLyricsMode by remember { mutableStateOf(false) }
    var isBoomMode by remember { mutableStateOf(false) }
    var shouldShowSynchronizedLyrics by remember { mutableStateOf(true) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showStoryCreatorSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showJamRoomDialog by remember { mutableStateOf(false) }
    val activeJamRoom by SwaaraListenTogether.currentRoom.collectAsState()
    var audioQualityIndex by remember { mutableIntStateOf(0) }
    val audioQualities = remember { listOf("320 KBPS • HQ AUDIO", "192 KBPS • HIGH", "128 KBPS • SAVER") }
    var totalDrag by remember { mutableFloatStateOf(0f) }
    var isDownloaded by remember(videoId, DataPreferences.downloadedSongIds) {
        mutableStateOf(videoId != null && DataPreferences.isSongExplicitlyDownloaded(videoId) && binder.isCached(videoId))
    }

    val downloadProgressMap by SwaaraFastDownloader.downloadProgress.collectAsState()
    val currentTrackProgress = videoId?.let { downloadProgressMap[it] } ?: -1
    val isCurrentlyDownloading = currentTrackProgress in 0..99

    LaunchedEffect(videoId, currentPosition, DataPreferences.downloadedSongIds) {
        if (videoId != null) {
            isDownloaded = DataPreferences.isSongExplicitlyDownloaded(videoId) && binder.isCached(videoId)
        }
    }

    // Realtime Live Jam Host Playback Sync
    LaunchedEffect(activeJamRoom, videoId, isPlaying, (currentPosition / 3000)) {
        activeJamRoom?.let { room ->
            if (room.isHost && !videoId.isNullOrBlank()) {
                SwaaraListenTogether.updateHostPlayback(
                    roomCode = room.roomCode,
                    songId = videoId!!,
                    title = title,
                    artist = artist,
                    artworkUrl = artworkUri,
                    isPlaying = isPlaying,
                    positionMs = currentPosition
                )
            }
        }
    }

    // Realtime Live Jam Listener Playback Sync
    LaunchedEffect(activeJamRoom) {
        val room = activeJamRoom
        if (room != null && !room.isHost) {
            SwaaraListenTogether.startListenerSync(
                scope = coroutineScope,
                roomCode = room.roomCode,
                onPlaybackSyncRequired = { songId, trackTitle, trackArtist, trackArt, shouldPlay, position ->
                    if (videoId != songId) {
                        binder.playTrack(
                            videoId = songId,
                            title = trackTitle,
                            artist = trackArtist,
                            thumbnailUrl = trackArt
                        )
                    }
                    if (kotlin.math.abs(currentPosition - position) > 3000) {
                        player.seekTo(position)
                    }
                    if (shouldPlay && !player.shouldBePlaying) {
                        player.play()
                    } else if (!shouldPlay && player.shouldBePlaying) {
                        player.pause()
                    }
                }
            )
        }
    }

    // Crowdsourced Community Play Reporting (played > 15s)
    LaunchedEffect(videoId) {
        if (!videoId.isNullOrBlank()) {
            delay(15000)
            SwaaraCommunityCharts.reportSongPlay(
                songId = videoId!!,
                title = title,
                artist = artist,
                artworkUrl = artworkUri
            )
        }
    }

    val navigateToArtist: () -> Unit = {
        coroutineScope.launch {
            val targetVideoId = videoId
            val targetArtistName = artist
            val browseId = withContext(Dispatchers.IO) {
                // 1. Check local DB
                if (!targetVideoId.isNullOrBlank()) {
                    val dbArtists = runCatching { Database.songArtistInfo(targetVideoId) }.getOrNull()
                    val firstId = dbArtists?.firstOrNull()?.id
                    if (!firstId.isNullOrBlank()) return@withContext firstId
                }
                // 2. Search online on YouTube Music
                if (targetArtistName.isNotBlank() && targetArtistName != "Unknown Artist") {
                    val searchResult = runCatching {
                        container.searchResultRepository.searchArtists(targetArtistName.trim(), null)?.getOrNull()
                    }.getOrNull()
                    val artistKey = searchResult?.items?.firstOrNull()?.key
                    if (!artistKey.isNullOrBlank()) return@withContext artistKey
                }
                null
            }

            if (!browseId.isNullOrBlank()) {
                onDismiss()
                artistRoute.global(browseId)
            } else {
                withContext(Dispatchers.Main) {
                    context.toast("Could not locate artist page")
                }
            }
        }
    }

    val navigateToAlbum: () -> Unit = {
        coroutineScope.launch {
            val targetVideoId = videoId
            val targetTitle = title
            val targetArtistName = artist
            val browseId = withContext(Dispatchers.IO) {
                // 1. Check local DB
                if (!targetVideoId.isNullOrBlank()) {
                    val dbAlbum = runCatching { Database.songAlbumInfo(targetVideoId) }.getOrNull()
                    val firstId = dbAlbum?.firstOrNull()?.id
                    if (!firstId.isNullOrBlank()) return@withContext firstId
                }
                // 2. Search online on YouTube Music
                if (targetTitle.isNotBlank() && targetTitle != "No Song Playing") {
                    val searchResult = runCatching {
                        container.searchResultRepository.searchAlbums("$targetTitle $targetArtistName".trim(), null)?.getOrNull()
                    }.getOrNull()
                    val albumKey = searchResult?.items?.firstOrNull()?.key
                    if (!albumKey.isNullOrBlank()) return@withContext albumKey
                }
                null
            }

            if (!browseId.isNullOrBlank()) {
                onDismiss()
                albumRoute.global(browseId)
            } else {
                withContext(Dispatchers.Main) {
                    context.toast("Could not locate album")
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showEqSheet) {
        RaagaEqualizerSheet(
            audioSessionId = player.audioSessionId,
            onDismiss = { showEqSheet = false }
        )
    }

    if (showSleepTimerDialog) {
        RaagaSleepTimerDialog(
            millisLeft = sleepTimerMillisLeft,
            onSetTimer = { delayMs -> binder.startSleepTimer(delayMs) },
            onCancelTimer = { binder.cancelSleepTimer() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    if (showStoryCreatorSheet) {
        SwaaraStoryCreatorSheet(
            title = title,
            artist = artist,
            artworkUrl = artworkUri,
            songDurationMs = duration,
            player = player,
            videoId = videoId,
            onDismiss = { showStoryCreatorSheet = false }
        )
    }

    if (showJamRoomDialog) {
        SwaaraJamRoomDialog(onDismiss = { showJamRoomDialog = false })
    }

    // Swaara Boom Audio Engine (Sub-bass 1000% + 3D Surround Acoustics)
    SwaaraBoomAudioEngine(
        audioSessionId = player.audioSessionId,
        isBoomActive = isBoomMode
    )

    // Contextual "More" Bottom Sheet
    if (showMoreSheet) {
        val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
            containerColor = SwaaraDeepSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Track Options",
                    color = SwaaraPrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Add to Playlist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showAddToPlaylistSheet = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.playlist),
                        contentDescription = null,
                        tint = SwaaraMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Add to Playlist",
                            color = SwaaraPrimaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Save this track to your custom playlists",
                            color = SwaaraSecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                // See Artist Profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            navigateToArtist()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = null,
                        tint = SwaaraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "See Artist Profile",
                            color = SwaaraPrimaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = artist,
                            color = SwaaraSecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                // View Full Album
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            navigateToAlbum()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.disc),
                        contentDescription = null,
                        tint = SwaaraViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "View Full Album",
                            color = SwaaraPrimaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Explore complete tracklist & releases",
                            color = SwaaraSecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                // Studio Equalizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showEqSheet = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.musical_notes),
                        contentDescription = null,
                        tint = SwaaraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Studio Equalizer",
                        color = SwaaraPrimaryText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share Story Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showStoryCreatorSheet = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.share_social),
                        contentDescription = null,
                        tint = SwaaraMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Share Story Card",
                        color = SwaaraPrimaryText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Listen Together (Live Jam)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showJamRoomDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.radio),
                        contentDescription = null,
                        tint = SwaaraViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Listen Together (Live Jam)",
                            color = SwaaraPrimaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (activeJamRoom != null) "Active Jam: ${activeJamRoom!!.roomCode}" else "Sync real-time playback across multiple devices",
                            color = SwaaraCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                // Sleep Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showMoreSheet = false
                            showSleepTimerDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.time),
                        contentDescription = null,
                        tint = SwaaraViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Sleep Timer",
                        color = SwaaraPrimaryText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isDownloaded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showMoreSheet = false
                                videoId?.let { id ->
                                    DataPreferences.removeDownloadedSongId(id)
                                    binder.removeDownloadedSong(id)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        Database.song(id).firstOrNull()?.let { Database.delete(it) }
                                    }
                                    isDownloaded = false
                                    context.toast("Removed \"$title\" from downloads")
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.trash),
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Remove from Downloads",
                            color = Color(0xFFFF5252),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAddToPlaylistSheet) {
        RaagaAddToPlaylistSheet(
            mediaItem = currentMediaItem,
            onDismiss = { showAddToPlaylistSheet = false },
            onCreatedOrAdded = { playlistName ->
                context.toast("Added to \"$playlistName\"")
            }
        )
    }

    // Periodic Progress Updater
    LaunchedEffect(player) {
        while (true) {
            isPlaying = player.shouldBePlaying
            isLoading = player.playbackState == Player.STATE_BUFFERING
            currentMediaItem = player.currentMediaItem
            currentPosition = player.currentPosition
            duration = player.duration.coerceAtLeast(1L)
            delay(300)
        }
    }

    // Draggable Play Queue Modal Bottom Sheet
    if (showQueueSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = SwaaraDeepSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Play Queue",
                    color = SwaaraPrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val timeline = player.currentTimeline
                val queueSize = timeline.windowCount
                val mediaItems = remember(queueSize, player.currentMediaItemIndex) {
                    (0 until queueSize).mapNotNull { idx ->
                        runCatching { player.getMediaItemAt(idx) }.getOrNull()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(mediaItems) { idx, item ->
                        val isCurrent = idx == player.currentMediaItemIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) SwaaraElevatedSurface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isCurrent) SwaaraMagenta.copy(alpha = 0.40f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    player.seekToDefaultPosition(idx)
                                    player.play()
                                    showQueueSheet = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.mediaMetadata.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.mediaMetadata.title?.toString() ?: "Track",
                                    color = if (isCurrent) SwaaraMagenta else SwaaraPrimaryText,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.mediaMetadata.artist?.toString()?.replace(Regex("[,&\\s]{2,}"), ", ") ?: "Artist",
                                    color = SwaaraSecondaryText,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    painter = painterResource(id = R.drawable.volume_up),
                                    contentDescription = "Playing",
                                    tint = SwaaraMagenta,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    SwaaraAmbientBackground(
        artworkUri = artworkUri,
        modifier = modifier
    ) {
        // Main Foreground Content (Responsive Edge-to-Edge with Center Alignment)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            val screenHeight = maxHeight
            val screenWidth = maxWidth
            val isCompactOrLandscape = isLandscape || screenHeight < 560.dp

            // Dynamic artwork sizing smoothly calculated from available height
            val fixedControlsHeight = 310.dp
            val availableForArtwork = (screenHeight - fixedControlsHeight).coerceAtLeast(180.dp)
            val dynamicArtworkSize = if (isLandscape) 180.dp
                else (availableForArtwork * 0.72f).coerceIn(190.dp, 330.dp).coerceAtMost(screenWidth - 44.dp)
            val dynamicAuraSize = dynamicArtworkSize + 16.dp

            if (!isCompactOrLandscape) {
                // Centered Column constrained to ideal max width (430dp) so controls don't overspread on wider phones
                Column(
                    modifier = Modifier
                        .widthIn(max = 430.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Sleek Unified Top Bar (Back, Player/Lyrics Capsule, Share)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SwaaraElevatedSurface.copy(alpha = 0.75f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_back),
                                contentDescription = "Back",
                                tint = SwaaraPrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Player / Lyrics Segment Capsule
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SwaaraDeepSurface.copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                                .padding(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Player Tab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (!isLyricsMode) SwaaraMagenta.copy(alpha = 0.22f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (!isLyricsMode) SwaaraMagenta.copy(alpha = 0.55f)
                                            else Color.Transparent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { isLyricsMode = false }
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.disc),
                                            contentDescription = null,
                                            tint = if (!isLyricsMode) SwaaraMagenta else SwaaraSecondaryText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Player",
                                            color = if (!isLyricsMode) SwaaraPrimaryText else SwaaraSecondaryText,
                                            fontSize = 12.sp,
                                            fontWeight = if (!isLyricsMode) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }

                                // Lyrics Tab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isLyricsMode) SwaaraMagenta.copy(alpha = 0.22f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isLyricsMode) SwaaraMagenta.copy(alpha = 0.55f)
                                            else Color.Transparent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { isLyricsMode = true }
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.musical_notes),
                                            contentDescription = null,
                                            tint = if (isLyricsMode) SwaaraMagenta else SwaaraSecondaryText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Lyrics",
                                            color = if (isLyricsMode) SwaaraPrimaryText else SwaaraSecondaryText,
                                            fontSize = 12.sp,
                                            fontWeight = if (isLyricsMode) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Action Buttons (Add to Playlist & Share Story)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showAddToPlaylistSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SwaaraElevatedSurface.copy(alpha = 0.75f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.bookmark_outline),
                                    contentDescription = "Add to Playlist",
                                    tint = SwaaraPrimaryText,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = { showStoryCreatorSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SwaaraElevatedSurface.copy(alpha = 0.75f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.share_social),
                                    contentDescription = "Share Story",
                                    tint = SwaaraPrimaryText,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Live Jam Room Active Badge (if in a session)
                    if (activeJamRoom != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SwaaraViolet.copy(alpha = 0.25f))
                                .border(1.dp, SwaaraViolet.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { showJamRoomDialog = true }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.radio),
                                    contentDescription = null,
                                    tint = SwaaraViolet,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Jam: ${activeJamRoom!!.roomCode}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1.0f))

                    // 2. Large Album Artwork with Ambient Glow
                    if (!isLyricsMode) {
                        Box(
                            modifier = Modifier.size(dynamicAuraSize),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(dynamicArtworkSize + 16.dp)
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    SwaaraViolet.copy(alpha = 0.32f),
                                                    SwaaraMagenta.copy(alpha = 0.15f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            // Artwork Container with Swipe Gestures
                            Box(
                                modifier = Modifier
                                    .size(dynamicArtworkSize)
                                    .shadow(
                                        elevation = 28.dp,
                                        shape = RoundedCornerShape(26.dp),
                                        ambientColor = Color.Black.copy(alpha = 0.70f),
                                        spotColor = SwaaraViolet.copy(alpha = 0.40f)
                                    )
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(SwaaraElevatedSurface)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(26.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                            onDragStart = { totalDrag = 0f },
                                            onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                                            onDragEnd = {
                                                if (totalDrag < -80f) player.forceSeekToNext()
                                                else if (totalDrag > 80f) player.forceSeekToPrevious()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    SwaaraDeepSurface,
                                                    SwaaraElevatedSurface,
                                                    Color(0xFF222234),
                                                    SwaaraElevatedSurface,
                                                    SwaaraDeepSurface
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(CircleShape)
                                                .background(SwaaraDeepSurface)
                                                .border(1.dp, SwaaraMagenta.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.musical_notes),
                                                contentDescription = null,
                                                tint = SwaaraMagenta,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                }

                                AsyncImage(
                                    model = artworkUri ?: if (!videoId.isNullOrBlank()) "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" else null,
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }
                    } else {
                        // Synchronized Karaoke Lyrics View
                        RaagaGlassContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dynamicAuraSize.coerceAtLeast(260.dp))
                                .padding(2.dp),
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = SwaaraDeepSurface.copy(alpha = 0.92f),
                            borderColor = SwaaraMagenta.copy(alpha = 0.35f)
                        ) {
                            if (videoId != null && currentMediaItem != null) {
                                Lyrics(
                                    mediaId = videoId,
                                    isDisplayed = true,
                                    onDismiss = { isLyricsMode = false },
                                    mediaMetadataProvider = { currentMediaItem!!.mediaMetadata },
                                    durationProvider = { player.duration.takeIf { it > 0 } ?: C.TIME_UNSET },
                                    ensureSongInserted = { currentMediaItem?.let { Database.insert(it) } },
                                    shouldShowSynchronizedLyrics = shouldShowSynchronizedLyrics,
                                    setShouldShowSynchronizedLyrics = { shouldShowSynchronizedLyrics = it },
                                    modifier = Modifier.fillMaxSize().padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1.0f))

                    // 3. Song Information (Title, Artist Pill, and Audio Quality Glass Capsule)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            color = SwaaraPrimaryText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Clickable Artist Capsule
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = navigateToArtist)
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = null,
                                    tint = SwaaraCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = artist,
                                    color = SwaaraPrimaryText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.chevron_forward),
                                    contentDescription = "See Artist",
                                    tint = SwaaraCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Audio Quality Glass Capsule (Click to cycle bitrates)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SwaaraDeepSurface.copy(alpha = 0.85f))
                                    .border(1.dp, SwaaraViolet.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        audioQualityIndex = (audioQualityIndex + 1) % audioQualities.size
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = audioQualities[audioQualityIndex],
                                    color = SwaaraViolet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1.0f))

                    // 4. Progress / Scrub Bar
                    RaagaFuturisticScrubBar(
                        currentPosition = currentPosition,
                        duration = duration,
                        onSeek = { targetMs -> player.seekTo(targetMs) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Primary Playback Controls (Perfect 5-Column Grid Alignment)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Col 1: Shuffle
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { player.shuffleModeEnabled = !player.shuffleModeEnabled },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shuffle),
                                    contentDescription = "Shuffle",
                                    tint = if (player.shuffleModeEnabled) SwaaraMagenta else SwaaraSecondaryText,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }

                        // Col 2: Previous
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { player.forceSeekToPrevious() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.play_skip_back),
                                    contentDescription = "Previous",
                                    tint = SwaaraPrimaryText,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Col 3: Center Dominant Play/Pause (68dp)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            RaagaPulsingPlayButton(
                                isPlaying = isPlaying,
                                isLoading = isLoading,
                                onClick = {
                                    if (player.shouldBePlaying) player.pause() else player.play()
                                },
                                size = 68
                            )
                        }

                        // Col 4: Next
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { player.forceSeekToNext() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.play_skip_forward),
                                    contentDescription = "Next",
                                    tint = SwaaraPrimaryText,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Col 5: Futuristic Animated Loop Button
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            RaagaAnimatedLoopButton(
                                repeatMode = player.repeatMode,
                                onClick = {
                                    player.repeatMode = when (player.repeatMode) {
                                        Player.REPEAT_MODE_OFF -> {
                                            context.toast("Repeat all tracks")
                                            Player.REPEAT_MODE_ALL
                                        }
                                        Player.REPEAT_MODE_ALL -> {
                                            context.toast("Repeat current track")
                                            Player.REPEAT_MODE_ONE
                                        }
                                        else -> {
                                            context.toast("Repeat off")
                                            Player.REPEAT_MODE_OFF
                                        }
                                    }
                                },
                                size = 48.dp,
                                iconSize = 23.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6. Secondary / Utility Controls (Matching 5-Column Grid Alignment)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Col 1: Queue
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showQueueSheet = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.playlist),
                                    contentDescription = "Queue",
                                    tint = SwaaraSecondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Col 2: Download / Offline Save Button
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    currentMediaItem?.let { mediaItem ->
                                        if (!isDownloaded && !isCurrentlyDownloading) {
                                            SwaaraFastDownloader.download(
                                                context = context,
                                                mediaItem = mediaItem,
                                                onFinished = { success ->
                                                    if (success) isDownloaded = true
                                                }
                                            )
                                        } else if (isCurrentlyDownloading) {
                                            context.toast("Downloading \"$title\" ($currentTrackProgress%)...")
                                        } else {
                                            context.toast("\"$title\" is saved offline in HQ (320 kbps)")
                                        }
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                if (isCurrentlyDownloading) {
                                    CircularProgressIndicator(
                                        progress = { (currentTrackProgress.coerceAtLeast(5)) / 100f },
                                        modifier = Modifier.size(20.dp),
                                        color = SwaaraCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.download),
                                        contentDescription = "Download Offline",
                                        tint = if (isDownloaded) SwaaraCyan else SwaaraSecondaryText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Col 3: Like / Favorite
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    currentMediaItem?.mediaId?.let { id ->
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val liked = Database.likedAtNow(id)
                                            Database.like(id, if (liked == null) System.currentTimeMillis() else null)
                                        }
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isLiked) R.drawable.heart else R.drawable.heart_outline),
                                    contentDescription = "Favorite",
                                    tint = if (isLiked) SwaaraMagenta else SwaaraSecondaryText,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }

                        // Col 4: Sleep Timer
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showSleepTimerDialog = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.time),
                                    contentDescription = "Sleep Timer",
                                    tint = if (isSleepTimerActive) SwaaraMagenta else SwaaraSecondaryText,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        // Col 5: More Options (Contextual Actions)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showMoreSheet = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ellipsis_horizontal),
                                    contentDescription = "More Options",
                                    tint = SwaaraSecondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Scrollable Column for Landscape or Ultra-Compact Screens
                Column(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.Center)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SwaaraElevatedSurface.copy(alpha = 0.75f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_back),
                                contentDescription = "Back",
                                tint = SwaaraPrimaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(SwaaraDeepSurface.copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                                .padding(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (!isLyricsMode) SwaaraMagenta.copy(alpha = 0.22f) else Color.Transparent)
                                        .clickable { isLyricsMode = false }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Player",
                                        color = if (!isLyricsMode) SwaaraPrimaryText else SwaaraSecondaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isLyricsMode) SwaaraMagenta.copy(alpha = 0.22f) else Color.Transparent)
                                        .clickable { isLyricsMode = true }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Lyrics",
                                        color = if (isLyricsMode) SwaaraPrimaryText else SwaaraSecondaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showStoryCreatorSheet = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SwaaraElevatedSurface.copy(alpha = 0.75f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.share_social),
                                contentDescription = "Share Story",
                                tint = SwaaraPrimaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isLyricsMode) {
                        Box(
                            modifier = Modifier
                                .size(dynamicArtworkSize)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SwaaraElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = artworkUri ?: if (!videoId.isNullOrBlank()) "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" else null,
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    } else {
                        RaagaGlassContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = SwaaraDeepSurface.copy(alpha = 0.92f),
                            borderColor = SwaaraMagenta.copy(alpha = 0.35f)
                        ) {
                            if (videoId != null && currentMediaItem != null) {
                                Lyrics(
                                    mediaId = videoId,
                                    isDisplayed = true,
                                    onDismiss = { isLyricsMode = false },
                                    mediaMetadataProvider = { currentMediaItem!!.mediaMetadata },
                                    durationProvider = { player.duration.takeIf { it > 0 } ?: C.TIME_UNSET },
                                    ensureSongInserted = { currentMediaItem?.let { Database.insert(it) } },
                                    shouldShowSynchronizedLyrics = shouldShowSynchronizedLyrics,
                                    setShouldShowSynchronizedLyrics = { shouldShowSynchronizedLyrics = it },
                                    modifier = Modifier.fillMaxSize().padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title & Artist
                    Text(
                        text = title,
                        color = SwaaraPrimaryText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = artist,
                        color = SwaaraSecondaryText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RaagaFuturisticScrubBar(
                        currentPosition = currentPosition,
                        duration = duration,
                        onSeek = { player.seekTo(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { player.shuffleModeEnabled = !player.shuffleModeEnabled }) {
                                Icon(painterResource(id = R.drawable.shuffle), null, tint = if (player.shuffleModeEnabled) SwaaraMagenta else SwaaraSecondaryText, modifier = Modifier.size(22.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { player.forceSeekToPrevious() }) {
                                Icon(painterResource(id = R.drawable.play_skip_back), null, tint = SwaaraPrimaryText, modifier = Modifier.size(26.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RaagaPulsingPlayButton(isPlaying = isPlaying, isLoading = isLoading, onClick = { if (player.shouldBePlaying) player.pause() else player.play() }, size = 62)
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { player.forceSeekToNext() }) {
                                Icon(painterResource(id = R.drawable.play_skip_forward), null, tint = SwaaraPrimaryText, modifier = Modifier.size(26.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            RaagaAnimatedLoopButton(
                                repeatMode = player.repeatMode,
                                onClick = {
                                    player.repeatMode = when (player.repeatMode) {
                                        Player.REPEAT_MODE_OFF -> {
                                            context.toast("Repeat all tracks")
                                            Player.REPEAT_MODE_ALL
                                        }
                                        Player.REPEAT_MODE_ALL -> {
                                            context.toast("Repeat current track")
                                            Player.REPEAT_MODE_ONE
                                        }
                                        else -> {
                                            context.toast("Repeat off")
                                            Player.REPEAT_MODE_OFF
                                        }
                                    }
                                },
                                size = 44.dp,
                                iconSize = 22.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { showQueueSheet = true }) {
                                Icon(painterResource(id = R.drawable.playlist), null, tint = SwaaraSecondaryText, modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                currentMediaItem?.let { mediaItem ->
                                    if (!isDownloaded && !isCurrentlyDownloading) {
                                        SwaaraFastDownloader.download(context, mediaItem) { success -> if (success) isDownloaded = true }
                                    }
                                }
                            }) {
                                Icon(painterResource(id = R.drawable.download), null, tint = if (isDownloaded) SwaaraCyan else SwaaraSecondaryText, modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                currentMediaItem?.mediaId?.let { id ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val liked = Database.likedAtNow(id)
                                        Database.like(id, if (liked == null) System.currentTimeMillis() else null)
                                    }
                                }
                            }) {
                                Icon(painterResource(id = if (isLiked) R.drawable.heart else R.drawable.heart_outline), null, tint = if (isLiked) SwaaraMagenta else SwaaraSecondaryText, modifier = Modifier.size(21.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { showSleepTimerDialog = true }) {
                                Icon(painterResource(id = R.drawable.time), null, tint = if (isSleepTimerActive) SwaaraMagenta else SwaaraSecondaryText, modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { showMoreSheet = true }) {
                                Icon(painterResource(id = R.drawable.ellipsis_horizontal), null, tint = SwaaraSecondaryText, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaagaAnimatedLoopButton(
    repeatMode: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 23.dp
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var previousMode by remember { mutableIntStateOf(repeatMode) }

    LaunchedEffect(repeatMode) {
        if (repeatMode != previousMode) {
            rotationAngle += 360f
            previousMode = repeatMode
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "loop_rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (repeatMode != Player.REPEAT_MODE_OFF) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "loop_scale"
    )

    val activeColor = when (repeatMode) {
        Player.REPEAT_MODE_ONE -> SwaaraMagenta
        Player.REPEAT_MODE_ALL -> SwaaraCyan
        else -> SwaaraSecondaryText
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(scale)
        ) {
            // Glow aura for active repeat modes
            if (repeatMode != Player.REPEAT_MODE_OFF) {
                Box(
                    modifier = Modifier
                        .size(iconSize + 14.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.14f))
                )
            }

            // Rotating loop icon
            Icon(
                painter = painterResource(id = R.drawable.sync),
                contentDescription = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> "Repeat Current Track"
                    Player.REPEAT_MODE_ALL -> "Repeat All Tracks"
                    else -> "Repeat Off"
                },
                tint = activeColor,
                modifier = Modifier
                    .size(iconSize)
                    .rotate(animatedRotation)
            )

            // "1" badge for REPEAT_MODE_ONE
            if (repeatMode == Player.REPEAT_MODE_ONE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(SwaaraMagenta)
                        .border(0.8.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaagaAddToPlaylistSheet(
    mediaItem: MediaItem?,
    onDismiss: () -> Unit,
    onCreatedOrAdded: (playlistName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isCreatingNewPlaylist by remember { mutableStateOf(false) }

    val userPlaylists by Database.playlistPreviews(
        PlaylistSortBy.DateAdded,
        SortOrder.Descending
    ).collectAsState(initial = emptyList())

    if (isCreatingNewPlaylist) {
        TextFieldDialog(
            hintText = "Enter playlist name",
            onDismiss = { isCreatingNewPlaylist = false },
            onAccept = { name ->
                isCreatingNewPlaylist = false
                if (name.isNotBlank()) {
                    val trimmedName = name.trim()
                    coroutineScope.launch(Dispatchers.IO) {
                        val playlistId = Database.insert(Playlist(name = trimmedName))
                        mediaItem?.let { item ->
                            Database.insert(item)
                            Database.insert(
                                SongPlaylistMap(
                                    songId = item.mediaId,
                                    playlistId = playlistId,
                                    position = 0
                                )
                            )
                        }
                    }
                    onCreatedOrAdded(trimmedName)
                    onDismiss()
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SwaaraDeepSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Add to Playlist",
                        color = SwaaraPrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose a playlist or create a new one",
                        color = SwaaraSecondaryText,
                        fontSize = 12.sp
                    )
                }

                // New Playlist Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, SwaaraCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { isCreatingNewPlaylist = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "New Playlist",
                        tint = SwaaraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New",
                        color = SwaaraCyan,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Track Card
            mediaItem?.let { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SwaaraElevatedSurface.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val artUri = item.mediaMetadata.artworkUri?.toString()
                        ?: "https://i.ytimg.com/vi/${item.mediaId}/hqdefault.jpg"
                    AsyncImage(
                        model = artUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.mediaMetadata.title?.toString() ?: "Unknown Title",
                            color = SwaaraPrimaryText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                            color = SwaaraSecondaryText,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (userPlaylists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.playlist),
                            contentDescription = null,
                            tint = SwaaraSecondaryText.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No custom playlists found",
                            color = SwaaraSecondaryText,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SwaaraMagenta.copy(alpha = 0.15f))
                                .border(1.dp, SwaaraMagenta.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { isCreatingNewPlaylist = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Create First Playlist",
                                color = SwaaraMagenta,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userPlaylists) { preview ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    mediaItem?.let { item ->
                                        coroutineScope.launch(Dispatchers.IO) {
                                            Database.insert(item)
                                            Database.insert(
                                                SongPlaylistMap(
                                                    songId = item.mediaId,
                                                    playlistId = preview.playlist.id,
                                                    position = preview.songCount
                                                )
                                            )
                                        }
                                    }
                                    onCreatedOrAdded(preview.playlist.name)
                                    onDismiss()
                                }
                                .background(SwaaraElevatedSurface.copy(alpha = 0.35f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SwaaraMagenta.copy(alpha = 0.15f))
                                    .border(1.dp, SwaaraMagenta.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.playlist),
                                    contentDescription = null,
                                    tint = SwaaraMagenta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preview.playlist.name,
                                    color = SwaaraPrimaryText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${preview.songCount} ${if (preview.songCount == 1) "song" else "songs"}",
                                    color = SwaaraSecondaryText,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = "Add",
                                tint = SwaaraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
