package com.varshuai.a1swaara

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.work.Configuration
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
import com.kieronquinn.monetcompat.core.MonetCompat
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.repositories.AlbumRepository
import com.varshuai.a1swaara.repositories.ApiPipedPlaylistRepository
import com.varshuai.a1swaara.repositories.ApiSponsorBlockRepository
import com.varshuai.a1swaara.repositories.ArtistRepository
import com.varshuai.a1swaara.repositories.BuiltInPlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseAlbumRepository
import com.varshuai.a1swaara.repositories.DatabaseArtistRepository
import com.varshuai.a1swaara.repositories.DatabaseBuiltInPlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseLibraryRepository
import com.varshuai.a1swaara.repositories.DatabaseLocalPlaylistRepository
import com.varshuai.a1swaara.repositories.DatabaseMediaItemMenuRepository
import com.varshuai.a1swaara.repositories.DatabaseMediaLibraryRepository
import com.varshuai.a1swaara.repositories.DatabaseOnlineSearchRepository
import com.varshuai.a1swaara.repositories.DatabasePlayerRepository
import com.varshuai.a1swaara.repositories.DatabasePlaylistRepository
import com.varshuai.a1swaara.repositories.DatabasePrecacheRepository
import com.varshuai.a1swaara.repositories.DatabaseQueueRepository
import com.varshuai.a1swaara.repositories.PrecacheRepository
import com.varshuai.a1swaara.repositories.DatabaseSettingsRepository
import com.varshuai.a1swaara.repositories.DatabaseSongsRepository
import com.varshuai.a1swaara.repositories.DatabaseSyncSettingsRepository
import com.varshuai.a1swaara.repositories.DeepLinkRepository
import com.varshuai.a1swaara.repositories.DefaultDatabaseSettingsRepository
import com.varshuai.a1swaara.repositories.DefaultHomeRepository
import com.varshuai.a1swaara.repositories.DefaultPlayerLyricsRepository
import com.varshuai.a1swaara.repositories.HomeRepository
import com.varshuai.a1swaara.repositories.LibraryRepository
import com.varshuai.a1swaara.repositories.LocalPlaylistRepository
import com.varshuai.a1swaara.repositories.MediaItemMenuRepository
import com.varshuai.a1swaara.repositories.MediaLibraryRepository
import com.varshuai.a1swaara.repositories.MoodRepository
import com.varshuai.a1swaara.repositories.OnlineSearchRepository
import com.varshuai.a1swaara.repositories.PipedPlaylistRepository
import com.varshuai.a1swaara.repositories.PlayerLyricsRepository
import com.varshuai.a1swaara.repositories.PlayerRepository
import com.varshuai.a1swaara.repositories.PlaylistRepository
import com.varshuai.a1swaara.repositories.QueueRepository
import com.varshuai.a1swaara.repositories.SearchResultRepository
import com.varshuai.a1swaara.repositories.SongsRepository
import com.varshuai.a1swaara.repositories.SponsorBlockRepository
import com.varshuai.a1swaara.repositories.SyncSettingsRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeDeepLinkRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeMoodRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeSearchResultRepository
import com.varshuai.a1swaara.repositories.YoutubeMusicInnertubeYouTubeRadioDataSource
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.extractor.NewPipeExtractorClient
import com.varshuai.a1swaara.extractor.NewPipePoTokenProvider
import com.varshuai.a1swaara.service.ServiceNotifications
import com.varshuai.a1swaara.utils.YouTubeRadioDataSource
import com.varshuai.a1swaara.utils.addHttpTransport
import com.varshuai.a1swaara.utils.initializeHttpTransport
import com.varshuai.a1swaara.work.DatabaseAutoBackupWorker
import app.a1swaara.compose.persist.PersistMap
import app.a1swaara.compose.preferences.PreferencesHolder
import app.a1swaara.core.ui.utils.isAtLeastAndroid12
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor

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
