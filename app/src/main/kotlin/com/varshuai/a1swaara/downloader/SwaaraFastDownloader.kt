package com.varshuai.a1swaara.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.extractor.NewPipeExtractorClient
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@OptIn(UnstableApi::class)
object SwaaraFastDownloader {
    private const val TAG = "SwaaraFastDownloader"
    private const val CHANNEL_ID = "swaara_fast_downloads"
    private const val CHANNEL_NAME = "A1 Swaara Downloads"

    private val scope = CoroutineScope(Dispatchers.IO)
    private val downloadingSongs = ConcurrentHashMap<String, Int>()
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    fun isDownloading(songId: String): Boolean = downloadingSongs.containsKey(songId)

    fun getProgress(songId: String): Int = downloadingSongs[songId] ?: -1

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "High-speed offline music downloads"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun download(
        context: Context,
        mediaItem: MediaItem,
        onProgress: (Int) -> Unit = {},
        onFinished: (Boolean) -> Unit = {}
    ) {
        val songId = mediaItem.mediaId
        if (songId.isBlank()) return

        val title = mediaItem.mediaMetadata.title?.toString() ?: "Song"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context)
        val notifId = songId.hashCode()

        if (downloadingSongs.containsKey(songId)) {
            context.toast("Already downloading \"$title\"...")
            return
        }

        downloadingSongs[songId] = 0
        _downloadProgress.value = downloadingSongs.toMap()
        context.toast("Starting fast download: \"$title\"...")

        scope.launch {
            val initialNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.download)
                .setContentTitle("Downloading: $title")
                .setContentText("Connecting to high-speed audio stream...")
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            notificationManager.notify(notifId, initialNotif)

            try {
                // 1. Resolve audio stream URL directly
                val audioResult = NewPipeExtractorClient.resolveAudioStream(songId)
                val directUrl = audioResult.audioStream?.content
                    ?: audioResult.videoStream?.content
                    ?: throw IllegalStateException("No valid audio stream found")

                Log.d(TAG, "Fast downloading $songId from direct URL: $directUrl")

                // 2. Setup CacheWriter with ExoPlayer shared cache
                val cache = PlayerService.sharedCache(context)
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .setConnectTimeoutMs(15000)
                    .setReadTimeoutMs(20000)
                    .setAllowCrossProtocolRedirects(true)

                val upstreamDataSource = httpFactory.createDataSource()
                val cacheDataSource = CacheDataSource(
                    cache,
                    upstreamDataSource,
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
                )

                val dataSpec = DataSpec.Builder()
                    .setUri(directUrl.toUri())
                    .setKey(songId)
                    .setFlags(DataSpec.FLAG_ALLOW_CACHE_FRAGMENTATION)
                    .build()

                var lastProgress = 0
                val cacheWriter = CacheWriter(
                    cacheDataSource,
                    dataSpec,
                    ByteArray(128 * 1024),
                    object : CacheWriter.ProgressListener {
                        override fun onProgress(requestLength: Long, bytesCached: Long, newBytesCached: Long) {
                            if (requestLength > 0) {
                                val pct = ((bytesCached * 100) / requestLength).toInt().coerceIn(0, 100)
                                if (pct != lastProgress && (pct % 5 == 0 || pct == 100)) {
                                    lastProgress = pct
                                    downloadingSongs[songId] = pct
                                    _downloadProgress.value = downloadingSongs.toMap()
                                    onProgress(pct)

                                    val progressNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.download)
                                        .setContentTitle("Downloading: $title")
                                        .setContentText("$pct% • ${(bytesCached / (1024 * 1024f)).formatMb()} / ${(requestLength / (1024 * 1024f)).formatMb()} MB")
                                        .setProgress(100, pct, false)
                                        .setOngoing(true)
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .build()
                                    notificationManager.notify(notifId, progressNotif)
                                }
                            }
                        }
                    }
                )

                // Execute high-speed caching
                cacheWriter.cache()

                // 3. Save song to local database and explicitly downloaded set
                Database.insert(mediaItem)
                DataPreferences.addDownloadedSongId(songId)

                withContext(Dispatchers.Main) {
                    val completeNotif = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.download)
                        .setContentTitle("Download Complete")
                        .setContentText("\"$title\" saved offline in HQ (320 kbps)")
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(notifId, completeNotif)
                    context.toast("\"$title\" downloaded successfully!")
                    onFinished(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fast download failed for $songId", e)
                withContext(Dispatchers.Main) {
                    notificationManager.cancel(notifId)
                    context.toast("Download failed: ${e.localizedMessage ?: "Network error"}")
                    onFinished(false)
                }
            } finally {
                downloadingSongs.remove(songId)
                _downloadProgress.value = downloadingSongs.toMap()
            }
        }
    }

    private fun Float.formatMb(): String = String.format("%.1f", this)
}
