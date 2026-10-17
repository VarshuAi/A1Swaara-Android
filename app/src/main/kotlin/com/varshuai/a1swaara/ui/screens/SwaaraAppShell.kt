package com.varshuai.a1swaara.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.ui.components.SwaaraMiniPlayer
import com.varshuai.a1swaara.ui.components.SwaaraNavigationBar
import com.varshuai.a1swaara.ui.screens.downloads.SwaaraDownloadsScreen
import com.varshuai.a1swaara.ui.screens.home.SwaaraHomeScreen
import com.varshuai.a1swaara.ui.screens.library.SwaaraLibraryScreen
import com.varshuai.a1swaara.ui.screens.player.SwaaraNowPlayingScreen
import com.varshuai.a1swaara.ui.screens.search.SwaaraSearchScreen
import com.varshuai.a1swaara.ui.screens.settings.SwaaraSettingsScreen
import com.varshuai.a1swaara.utils.forceSeekToNext
import com.varshuai.a1swaara.utils.forceSeekToPrevious
import com.varshuai.a1swaara.utils.shouldBePlaying
import app.a1swaara.compose.routing.RouteHandler
import com.varshuai.a1swaara.ui.screens.GlobalRoutes
import com.varshuai.a1swaara.ui.screens.localplaylist.LocalPlaylistScreen
import com.varshuai.a1swaara.ui.screens.localPlaylistRoute
import com.varshuai.a1swaara.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.updater.SwaaraForceUpdateDialog
import com.varshuai.a1swaara.updater.SwaaraUpdateManager
import com.varshuai.a1swaara.utils.toast
import com.varshuai.a1swaara.ui.screens.builtInPlaylistRoute
import com.varshuai.a1swaara.ui.screens.mood.MoodScreen
import com.varshuai.a1swaara.ui.screens.moodRoute
import kotlinx.coroutines.delay

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import app.a1swaara.core.ui.utils.songBundle

val LocalOpenNowPlaying = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun SwaaraAppShell() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val player = binder?.player

    val isUpdateAvailable by SwaaraUpdateManager.isUpdateAvailable.collectAsStateWithLifecycle()
    val isMandatoryLockActive by SwaaraUpdateManager.isMandatoryLockActive.collectAsStateWithLifecycle()
    val updateInfo by SwaaraUpdateManager.updateInfo.collectAsStateWithLifecycle()
    val latestRelease by SwaaraUpdateManager.latestRelease.collectAsStateWithLifecycle()
    var showEnforcedUpdateDialog by remember { mutableStateOf(false) }

    var isNowPlayingOpen by remember { mutableStateOf(false) }
    val openNowPlaying: () -> Unit = { isNowPlayingOpen = true }

    CompositionLocalProvider(LocalOpenNowPlaying provides openNowPlaying) {
        RouteHandler(listenToGlobalEmitter = true) {
            GlobalRoutes()

            localPlaylistRoute { playlistId ->
                LocalPlaylistScreen(playlistId = playlistId)
            }

            builtInPlaylistRoute { builtInPlaylist ->
                BuiltInPlaylistScreen(builtInPlaylist = builtInPlaylist)
            }

            moodRoute { mood ->
                MoodScreen(mood = mood)
            }

            Content {

        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val tabHistory = remember { mutableStateListOf(0) }

        // When update lock is active, force redirect to Downloads screen (tab 3)
        LaunchedEffect(isMandatoryLockActive, isUpdateAvailable) {
            if (isMandatoryLockActive && isUpdateAvailable) {
                selectedTabIndex = 3
                tabHistory.clear()
                tabHistory.add(3)
            }
        }

        var currentMediaItem by remember { mutableStateOf(player?.currentMediaItem) }
        var isPlaying by remember { mutableStateOf(player?.shouldBePlaying == true) }
        var isLoading by remember { mutableStateOf(player?.playbackState == Player.STATE_BUFFERING) }
        var progress by remember { mutableFloatStateOf(0f) }

        // Real-time track listener
        DisposableEffect(player) {
            val listener = object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    currentMediaItem = mediaItem
                    if (mediaItem != null && mediaItem.mediaMetadata.extras?.songBundle?.isFromPersistentQueue != true) {
                        isNowPlayingOpen = true
                    }
                }
                override fun onEvents(p: Player, events: Player.Events) {
                    currentMediaItem = p.currentMediaItem
                    isPlaying = p.shouldBePlaying
                    isLoading = p.playbackState == Player.STATE_BUFFERING
                }
            }
            player?.addListener(listener)
            onDispose {
                player?.removeListener(listener)
            }
        }

    // Polling loop for smooth progress bar and state sync
    LaunchedEffect(player) {
        while (true) {
            if (player != null) {
                isPlaying = player.shouldBePlaying
                isLoading = player.playbackState == Player.STATE_BUFFERING
                currentMediaItem = player.currentMediaItem
                val dur = player.duration
                val pos = player.currentPosition
                progress = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
            }
            delay(300)
        }
    }

    // Back handling: Tab history fallback -> Home Tab -> Exit
    BackHandler(enabled = isNowPlayingOpen || selectedTabIndex != 0 || tabHistory.size > 1) {
        if (isNowPlayingOpen) {
            isNowPlayingOpen = false
        } else if (tabHistory.size > 1) {
            tabHistory.removeAt(tabHistory.size - 1)
            selectedTabIndex = tabHistory.last()
        } else if (selectedTabIndex != 0) {
            tabHistory.clear()
            tabHistory.add(0)
            selectedTabIndex = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070B))
    ) {
        // Tab Content Screen
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            when (selectedTabIndex) {
                0 -> SwaaraHomeScreen(
                    onSearchClick = {
                        if (isUpdateAvailable && isMandatoryLockActive) {
                            context.toast("Online search is locked. Only downloaded songs can be played.")
                            selectedTabIndex = 3
                            showEnforcedUpdateDialog = true
                        } else {
                            tabHistory.add(1)
                            selectedTabIndex = 1
                        }
                    },
                    onSettingsClick = {
                        tabHistory.add(4)
                        selectedTabIndex = 4
                    }
                )
                1 -> SwaaraSearchScreen()
                2 -> SwaaraLibraryScreen()
                3 -> SwaaraDownloadsScreen()
                4 -> SwaaraSettingsScreen()
            }
        }

        // Floating Mini-Player & Bottom Navigation Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            if (currentMediaItem != null) {
                SwaaraMiniPlayer(
                    mediaItem = currentMediaItem!!,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    progress = progress,
                    onPlayPauseClick = {
                        if (player?.shouldBePlaying == true) player.pause() else player?.play()
                    },
                    onNextClick = { player?.forceSeekToNext() },
                    onPreviousClick = { player?.forceSeekToPrevious() },
                    onTap = { isNowPlayingOpen = true }
                )
            }

            SwaaraNavigationBar(
                selectedIndex = selectedTabIndex,
                onTabSelected = { newIndex ->
                    if (isUpdateAvailable && isMandatoryLockActive && newIndex in listOf(0, 1, 2)) {
                        context.toast("Online music is locked. Please update A1 Swaara or enjoy downloaded songs.")
                        selectedTabIndex = 3
                        showEnforcedUpdateDialog = true
                    } else {
                        if (selectedTabIndex != newIndex) {
                            tabHistory.removeAll { it == newIndex }
                            tabHistory.add(newIndex)
                            selectedTabIndex = newIndex
                        }
                    }
                }
            )
        }

        // Full Screen Now Playing Sheet Animation
        AnimatedVisibility(
            visible = isNowPlayingOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SwaaraNowPlayingScreen(
                onDismiss = { isNowPlayingOpen = false }
            )
        }

        // Enforced OTA Update Modal Dialog if user navigates away from Downloads
        if (showEnforcedUpdateDialog && isUpdateAvailable && (updateInfo != null || latestRelease != null)) {
            SwaaraForceUpdateDialog(
                release = latestRelease,
                updateInfo = updateInfo,
                onDismiss = {
                    showEnforcedUpdateDialog = false
                    selectedTabIndex = 3
                }
            )
        }
        }
    }
}
}
}

