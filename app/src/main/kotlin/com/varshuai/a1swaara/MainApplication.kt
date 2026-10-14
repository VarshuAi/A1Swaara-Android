package com.varshuai.a1swaara

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.work.Configuration
import com.varshuai.a1swaara.preferences.AppearancePreferences
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.repositories.ArtistRepository
import com.varshuai.a1swaara.repositories.AlbumRepository
import com.varshuai.a1swaara.repositories.BuiltInPlaylistRepository
import com.varshuai.a1swaara.repositories.ApiPipedPlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseAlbumRepository
import com.varshuai.a1swaara.repositories.DatabaseArtistRepository
import com.varshuai.a1swaara.repositories.DatabaseBuiltInPlaylistRepository
import com.varshuai.a1swaara.repositories.DefaultHomeRepository
import com.varshuai.a1swaara.repositories.DatabaseLibraryRepository
import com.varshuai.a1swaara.repositories.DatabaseOnlineSearchRepository
import com.varshuai.a1swaara.repositories.DatabasePlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseSongsRepository
import com.varshuai.a1swaara.repositories.DatabaseSettingsRepository
import com.varshuai.a1swaara.repositories.DatabaseMediaItemMenuRepository
import com.varshuai.a1swaara.repositories.DeepLinkRepository
import com.varshuai.a1swaara.repositories.HomeRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeDeepLinkRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeSearchResultRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeMoodRepository
import com.varshuai.a1swaara.repositories.LibraryRepository
import com.varshuai.a1swaara.repositories.MoodRepository
import com.varshuai.a1swaara.repositories.LocalPlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseLocalPlaylistRepository
import com.varshuai.a1swaara.repositories.MediaItemMenuRepository
import com.varshuai.a1swaara.repositories.MediaLibraryRepository
import com.varshuai.a1swaara.repositories.OnlineSearchRepository
import com.varshuai.a1swaara.repositories.PipedPlaylistRepository
import com.varshuai.a1swaara.repositories.PlaylistRepository
import com.varshuai.a1swaara.repositories.PlayerRepository
import com.varshuai.a1swaara.repositories.QueueRepository
import com.varshuai.a1swaara.repositories.DatabasePlayerRepository
import com.varshuai.a1swaara.repositories.DatabaseQueueRepository
import com.varshuai.a1swaara.repositories.PrecacheRepository
import com.varshuai.a1swaara.repositories.DatabasePrecacheRepository
import com.varshuai.a1swaara.repositories.DatabaseMediaLibraryRepository
import com.varshuai.a1swaara.repositories.SearchResultRepository
import com.varshuai.a1swaara.repositories.SongsRepository
import com.varshuai.a1swaara.repositories.SponsorBlockRepository
import com.varshuai.a1swaara.repositories.SyncSettingsRepository
import com.varshuai.a1swaara.repositories.DatabaseSyncSettingsRepository
import com.varshuai.a1swaara.repositories.DefaultDatabaseSettingsRepository
import com.varshuai.a1swaara.repositories.PlayerLyricsRepository
import com.varshuai.a1swaara.repositories.DefaultPlayerLyricsRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeYouTubeRadioDataSource
import com.varshuai.a1swaara.repositories.ApiSponsorBlockRepository
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.extractor.NewPipeExtractorClient
import com.varshuai.a1swaara.extractor.NewPipePoTokenProvider
import com.varshuai.a1swaara.service.ServiceNotifications
import com.varshuai.a1swaara.service.downloadState
import com.varshuai.a1swaara.ui.components.BottomSheetMenu
import com.varshuai.a1swaara.ui.components.rememberBottomSheetState
import com.varshuai.a1swaara.ui.components.themed.LinearProgressIndicator
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute
import com.varshuai.a1swaara.ui.screens.home.HomeScreen
import com.varshuai.a1swaara.ui.screens.onboarding.RaagaRootNavHost
import com.varshuai.a1swaara.ui.screens.player.Player
import com.varshuai.a1swaara.ui.screens.player.Thumbnail
import com.varshuai.a1swaara.ui.screens.playlistRoute
import com.varshuai.a1swaara.ui.screens.searchResultRoute
import com.varshuai.a1swaara.ui.screens.settingsRoute
import com.varshuai.a1swaara.utils.DisposableListener
import com.varshuai.a1swaara.utils.KeyedCrossfade
import com.varshuai.a1swaara.utils.LocalMonetCompat
import com.varshuai.a1swaara.utils.LocalPlaybackActions
import com.varshuai.a1swaara.utils.collectProvidedBitmapAsState
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.intent
import com.varshuai.a1swaara.utils.addHttpTransport
import com.varshuai.a1swaara.utils.initializeHttpTransport
import com.varshuai.a1swaara.utils.invokeOnReady
import com.varshuai.a1swaara.utils.isInPip
import com.varshuai.a1swaara.utils.maybeEnterPip
import com.varshuai.a1swaara.utils.maybeExitPip
import com.varshuai.a1swaara.utils.rememberPlaybackActions
import com.varshuai.a1swaara.utils.setDefaultPalette
import com.varshuai.a1swaara.utils.shouldBePlaying
import com.varshuai.a1swaara.utils.toast
import com.varshuai.a1swaara.utils.YouTubeRadioDataSource
import com.varshuai.a1swaara.work.DatabaseAutoBackupWorker
import app.a1swaara.compose.persist.LocalPersistMap
import app.a1swaara.compose.persist.PersistMap
import app.a1swaara.compose.preferences.PreferencesHolder
import app.a1swaara.core.ui.Darkness
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.SystemBarAppearance
import app.a1swaara.core.ui.amoled
import app.a1swaara.core.ui.appearance
import app.a1swaara.core.ui.rippleConfiguration
import app.a1swaara.core.ui.shimmerTheme
import app.a1swaara.core.ui.utils.activityIntentBundle
import app.a1swaara.core.ui.utils.isAtLeastAndroid12
import app.a1swaara.core.ui.utils.songBundle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.bitmapFactoryExifOrientationStrategy
import coil3.decode.ExifOrientationStrategy
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.kieronquinn.monetcompat.core.MonetActivityAccessException
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener
import com.valentinilk.shimmer.LocalShimmerTheme
import dev.kdrag0n.monet.theme.ColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor

private const val TAG = "MainActivity"
private val coroutineScope = CoroutineScope(Dispatchers.IO)

// Viewmodel in order to avoid recreating the entire Player state (WORKAROUND)
class MainViewModel : ViewModel() {
    var binder: PlayerService.Binder? by mutableStateOf(null)

    suspend fun awaitBinder(): PlayerService.Binder =
        binder ?: snapshotFlow { binder }.filterNotNull().first()
}

class MainActivity : ComponentActivity(), MonetColorsChangedListener {
    private val vm: MainViewModel by viewModels()
    private val appContainer get() = applicationContext.appContainer

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.Binder) vm.binder = service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            vm.binder = null
            // Try to rebind, otherwise fail
            unbindService(this)
            bindService(intent<PlayerService>(), this, Context.BIND_AUTO_CREATE)
        }
    }

    private var _monet: MonetCompat? by mutableStateOf(null)
    private val monet get() = _monet ?: throw MonetActivityAccessException()

    override fun onStart() {
        super.onStart()
        try {
            startService(intent<PlayerService>())
        } catch (_: Exception) {}
        bindService(intent<PlayerService>(), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        MonetCompat.setup(this)
        _monet = MonetCompat.getInstance()
        monet.setDefaultPalette()
        monet.addMonetColorsChangedListener(
            listener = this,
            notifySelf = false
        )
        monet.updateMonetColors()
        monet.invokeOnReady {
            setContent()
        }

        intent?.let { handleIntent(it) }
        addOnNewIntentListener(::handleIntent)

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.varshuai.a1swaara.cloud.SwaaraPushNotificationEngine.checkAndPostNotifications(this@MainActivity)
        }

        // Auto-Sync Library Observer: Schedules debounced cloud backup when favorites change
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var isInitial = true
            Database.favorites().collect {
                if (isInitial) {
                    isInitial = false
                } else {
                    com.varshuai.a1swaara.cloud.SwaaraCloudSync.scheduleAutoBackup(this@MainActivity)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppWrapper(
        modifier: Modifier = Modifier,
        content: @Composable BoxWithConstraintsScope.() -> Unit
    ) = with(AppearancePreferences) {
        val sampleBitmap = vm.binder.collectProvidedBitmapAsState()
        val appearance = appearance(
            source = colorSource,
            mode = colorMode,
            darkness = darkness,
            fontFamily = fontFamily,
            materialAccentColor = Color(monet.getAccentColor(this@MainActivity)),
            sampleBitmap = sampleBitmap,
            applyFontPadding = applyFontPadding,
            thumbnailRoundness = thumbnailRoundness.dp
        )

        SystemBarAppearance(palette = appearance.colorPalette)

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(appearance.colorPalette.background0)
        ) {
            val playbackActions = rememberPlaybackActions(vm.binder)
            CompositionLocalProvider(
                LocalAppearance provides appearance,
                LocalPlayerServiceBinder provides vm.binder,
                LocalPlaybackActions provides playbackActions,
                LocalAppContainer provides appContainer,
                LocalCredentialManager provides appContainer.credentialManager,
                LocalIndication provides ripple(),
                LocalRippleConfiguration provides rippleConfiguration(appearance = appearance),
                LocalShimmerTheme provides shimmerTheme(),
                LocalLayoutDirection provides LayoutDirection.Ltr,
                LocalPersistMap provides appContainer.application.persistMap,
                LocalMonetCompat provides monet
            ) {
                content()
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    @OptIn(ExperimentalLayoutApi::class)
    fun setContent() = setContent {
        AppWrapper {
            val density = LocalDensity.current
            val windowsInsets = WindowInsets.systemBars
            val bottomDp = with(density) { windowsInsets.getBottom(density).toDp() }

            val imeVisible = WindowInsets.isImeVisible
            val imeBottomDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }
            val animatedBottomDp by animateDpAsState(
                targetValue = if (imeVisible) 0.dp else bottomDp,
                label = ""
            )

            val playerBottomSheetState = rememberBottomSheetState(
                key = vm.binder,
                dismissedBound = 0.dp,
                collapsedBound = Dimensions.items.collapsedPlayerHeight + bottomDp,
                expandedBound = maxHeight
            )

            val playerAwareWindowInsets = remember(
                bottomDp,
                animatedBottomDp,
                playerBottomSheetState.value,
                imeVisible,
                imeBottomDp
            ) {
                val bottom =
                    if (imeVisible) imeBottomDp.coerceAtLeast(playerBottomSheetState.value)
                    else playerBottomSheetState.value.coerceIn(
                        animatedBottomDp..playerBottomSheetState.collapsedBound
                    )

                windowsInsets
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    .add(WindowInsets(bottom = bottom))
            }

            val pip = isInPip(
                onChange = {
                    if (!it || vm.binder?.player?.shouldBePlaying != true) return@isInPip
                    playerBottomSheetState.expandSoft()
                }
            )

            KeyedCrossfade(state = pip) { currentPip ->
                if (currentPip) Thumbnail(
                    isShowingLyrics = true,
                    onShowLyrics = { },
                    isShowingStatsForNerds = false,
                    onShowStatsForNerds = { },
                    onOpenDialog = { },
                    likedAt = null,
                    setLikedAt = { },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    shouldShowSynchronizedLyrics = true,
                    setShouldShowSynchronizedLyrics = { },
                    showLyricsControls = false
                ) else CompositionLocalProvider(
                    LocalPlayerAwareWindowInsets provides playerAwareWindowInsets
                ) {
                    val isDownloading by downloadState.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        RaagaRootNavHost()
                    }

                    AnimatedVisibility(
                        visible = isDownloading,
                        modifier = Modifier.padding(playerAwareWindowInsets.asPaddingValues())
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )
                    }

                    BottomSheetMenu(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            vm.binder?.player.DisposableListener {
                object : Player.Listener {
                    override fun onMediaItemTransition(
                        mediaItem: MediaItem?,
                        reason: Int
                    ) = when {
                        mediaItem == null -> {
                            maybeExitPip()
                            playerBottomSheetState.dismissSoft()
                        }

                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED &&
                                mediaItem.mediaMetadata.extras?.songBundle?.isFromPersistentQueue != true
                            -> playerBottomSheetState.expandSoft()

                        playerBottomSheetState.dismissed -> playerBottomSheetState.collapseSoft()

                        else -> Unit
                    }
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun handleIntent(intent: Intent) = lifecycleScope.launch(Dispatchers.IO) {
        val extras = intent.extras?.activityIntentBundle

        when (intent.action) {
            Intent.ACTION_SEARCH -> {
                val query = extras?.query ?: return@launch
                extras.query = null

                searchResultRoute.ensureGlobal(query)
            }

            Intent.ACTION_APPLICATION_PREFERENCES -> settingsRoute.ensureGlobal()

            Intent.ACTION_VIEW, Intent.ACTION_SEND -> {
                val uri = intent.data
                    ?: runCatching { extras?.text?.toUri() }.getOrNull()
                    ?: return@launch

                intent.data = null
                extras?.text = null

                handleUrl(uri, vm.awaitBinder())
            }

            MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> {
                val query = when (extras?.mediaFocus) {
                    null, "vnd.android.cursor.item/*" -> extras?.query ?: extras?.text
                    MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE -> extras.genre
                    MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> extras.artist
                    MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> extras.album
                    "vnd.android.cursor.item/audio" -> listOfNotNull(
                        extras.album,
                        extras.artist,
                        extras.genre,
                        extras.title
                    ).joinToString(separator = " ")

                    @Suppress("deprecation")
                    MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE
                    -> extras.playlist

                    else -> null
                }

                if (!query.isNullOrBlank()) vm.awaitBinder().playFromSearch(query)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        monet.removeMonetColorsChangedListener(this)
        _monet = null

        removeOnNewIntentListener(::handleIntent)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onMonetColorsChanged(
        monet: MonetCompat,
        monetColors: ColorScheme,
        isInitialChange: Boolean
    ) {
        if (!isInitialChange) recreate()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (AppearancePreferences.autoPip && vm.binder?.player?.shouldBePlaying == true) maybeEnterPip()
    }
}

context(context: Context)
@Suppress("CyclomaticComplexMethod")
fun handleUrl(
    uri: Uri,
    binder: PlayerService.Binder?
) {
    val path = uri.pathSegments.firstOrNull()
    Log.d(TAG, "Opening url: $uri ($path)")

    coroutineScope.launch {
        when (path) {
            "search" -> uri.getQueryParameter("q")?.let { query ->
                searchResultRoute.ensureGlobal(query)
            }

            "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                val browseId = "VL$playlistId"

                if (playlistId.startsWith("OLAK5uy_")) context.appContainer.deepLinkRepository
                    .resolveAlbumBrowseIdFromPlaylist(browseId)
                    ?.let { albumRoute.ensureGlobal(it) } ?: withContext(Dispatchers.Main) {
                    context.toast(context.getString(R.string.error_url, uri))
                }
                else playlistRoute.ensureGlobal(
                    p0 = browseId,
                    p1 = uri.getQueryParameter("params"),
                    p2 = null,
                    p3 = playlistId.startsWith("RDCLAK5uy_")
                )
            }

            "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                artistRoute.ensureGlobal(channelId)
            }

            else -> when {
                path == "watch" -> uri.getQueryParameter("v")
                path == "song" -> uri.getQueryParameter("id") ?: uri.lastPathSegment?.takeIf { it != "song" }
                path == "s" || path == "track" -> uri.lastPathSegment?.takeIf { it != path }
                uri.host == "song" || uri.host == "watch" || uri.host == "s" -> uri.getQueryParameter("id") ?: uri.getQueryParameter("v") ?: uri.lastPathSegment
                uri.host == "youtu.be" -> path
                else -> {
                    withContext(Dispatchers.Main) {
                        context.toast(context.getString(R.string.error_url, uri))
                    }
                    null
                }
            }?.let { videoId ->
                context.appContainer.deepLinkRepository.resolveSongMediaItem(videoId)?.let { mediaItem ->
                    withContext(Dispatchers.Main) {
                        binder?.player?.forcePlay(mediaItem)
                    }
                }
            }
        }
    }
}

val LocalPlayerServiceBinder = staticCompositionLocalOf<PlayerService.Binder?> { null }
val LocalPlayerAwareWindowInsets =
    compositionLocalOf { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("LocalAppContainer is not provided")
}
val LocalCredentialManager = staticCompositionLocalOf<CredentialManager> {
    error("LocalCredentialManager is not provided")
}

class AppContainer(
    val application: MainApplication
) {
    val credentialManager: CredentialManager by lazy { CredentialManager.create(application) }
    val songsRepository: SongsRepository by lazy { DatabaseSongsRepository }
    val searchResultRepository: SearchResultRepository by lazy { YoutubeMusicInnertubeSearchResultRepository }
    val artistRepository: ArtistRepository by lazy { DatabaseArtistRepository }
    val libraryRepository: LibraryRepository by lazy { DatabaseLibraryRepository }
    val onlineSearchRepository: OnlineSearchRepository by lazy { DatabaseOnlineSearchRepository }
    val homeRepository: HomeRepository by lazy { DefaultHomeRepository }
    val builtInPlaylistRepository: BuiltInPlaylistRepository by lazy { DatabaseBuiltInPlaylistRepository }
    val albumRepository: AlbumRepository by lazy { DatabaseAlbumRepository }
    val playlistRepository: PlaylistRepository by lazy { DatabasePlaylistRepository }
    val pipedPlaylistRepository: PipedPlaylistRepository by lazy { ApiPipedPlaylistRepository }
    val moodRepository: MoodRepository by lazy { YoutubeMusicInnertubeMoodRepository }
    val localPlaylistRepository: LocalPlaylistRepository by lazy { DatabaseLocalPlaylistRepository }
    val syncSettingsRepository: SyncSettingsRepository by lazy { DatabaseSyncSettingsRepository }
    val playerRepository: PlayerRepository by lazy { DatabasePlayerRepository }
    val queueRepository: QueueRepository by lazy { DatabaseQueueRepository }
    val databaseSettingsRepository: DatabaseSettingsRepository by lazy { DefaultDatabaseSettingsRepository }
    val playerLyricsRepository: PlayerLyricsRepository by lazy { DefaultPlayerLyricsRepository }
    val mediaItemMenuRepository: MediaItemMenuRepository by lazy { DatabaseMediaItemMenuRepository }
    val deepLinkRepository: DeepLinkRepository by lazy { YoutubeMusicInnertubeDeepLinkRepository }
    val precacheRepository: PrecacheRepository by lazy { DatabasePrecacheRepository }
    val mediaLibraryRepository: MediaLibraryRepository by lazy { DatabaseMediaLibraryRepository }
    val youTubeRadioDataSource: YouTubeRadioDataSource by lazy { YoutubeMusicInnertubeYouTubeRadioDataSource }
    val sponsorBlockRepository: SponsorBlockRepository by lazy { ApiSponsorBlockRepository }

    fun initialize() {
        DatabaseInitializer(application.applicationContext)
        NewPipePoTokenProvider.initialize(application.applicationContext)
        YoutubeStreamExtractor.setPoTokenProvider(NewPipePoTokenProvider)
        YoutubeStreamExtractor.setFetchIosClient(false)
        NewPipeExtractorClient.ensureInitialized(application.applicationContext)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MainApplication).appContainer

class MainApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .let {
                    if (isAtLeastAndroid12) it.detectUnsafeIntentLaunch()
                    else it
                }
                .penaltyLog()
                .penaltyDeath()
                .build()
        )

        MonetCompat.debugLog = BuildConfig.DEBUG
        super.onCreate()

        // MonetCompat registers its process-wide wallpaper receiver with the
        // context passed to the first setup call. Initialize the singleton
        // here so it never retains a MainActivity after configuration changes
        // or process teardown.
        MonetCompat.enablePaletteCompat()
        MonetCompat.setup(applicationContext)

        MainApplicationProvider.application = this
        initializeHttpTransport(this)
        appContainer = AppContainer(this).also(AppContainer::initialize)
        ServiceNotifications.createAll(this)
        DatabaseAutoBackupWorker.upsert(this)
        com.varshuai.a1swaara.ui.screens.settings.VersionCheckWorker.upsert(this, DataPreferences.versionCheckPeriod.period)
        com.varshuai.a1swaara.work.SwaaraPushWorker.upsert(this)
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(this)
        .components {
            addHttpTransport(applicationContext)
        }
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.1)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("coil"))
                .maxSizeBytes(DataPreferences.coilDiskCacheMaxSize.bytes)
                .build()
        }
        .bitmapFactoryExifOrientationStrategy(ExifOrientationStrategy.IGNORE)
        .let { if (BuildConfig.DEBUG) it.logger(DebugLogger()) else it }
        .build()

    val persistMap = PersistMap()

    override val workManagerConfiguration = Configuration.Builder()
        .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
        .build()
}

open class GlobalPreferencesHolder : PreferencesHolder(
    application = MainApplicationProvider.application,
    name = "preferences"
)

private object MainApplicationProvider {
    lateinit var application: MainApplication
}
