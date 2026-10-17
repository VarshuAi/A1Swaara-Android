@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.varshuai.a1swaara.service

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.extractor.DefaultExtractorsFactory
import com.varshuai.a1swaara.appContainer
import com.varshuai.a1swaara.extractor.NewPipeExtractorClient
import com.varshuai.a1swaara.extractor.YoutubeHttpDataSourceFactory
import com.varshuai.a1swaara.models.Format
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.preferences.PlayerPreferences
import com.varshuai.a1swaara.utils.ConditionalCacheDataSourceFactory
import com.varshuai.a1swaara.utils.asDataSource
import com.varshuai.a1swaara.utils.readOnlyWhen
import app.a1swaara.core.ui.utils.songBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import java.io.IOException
import java.util.LinkedHashMap

class NewPipeAudioMediaSourceFactory(
    private val context: Context,
    private val cache: Cache,
    private val findMediaItem: suspend (videoId: String) -> MediaItem?,
    private val dbWriteScope: CoroutineScope
) : MediaSource.Factory {
    private val playerRepository = context.appContainer.playerRepository
    private val dataSourceFactory = createDataSourceFactory(context, cache, ::resolveDashManifestUriForPlayback)
    private val delegate = DefaultMediaSourceFactory(
        dataSourceFactory,
        DefaultExtractorsFactory()
    )

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        val item = if (mediaItem.isLocal) {
            mediaItem
        } else {
            mediaItem.buildUpon()
                .setUri(mediaItem.mediaId)
                .setMimeType(null)
                .setCustomCacheKey(mediaItem.mediaId)
                .build()
        }

        return delegate.createMediaSource(item)
    }

    override fun getSupportedTypes(): IntArray = intArrayOf(
        C.CONTENT_TYPE_DASH,
        C.CONTENT_TYPE_OTHER
    )

    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): MediaSource.Factory {
        delegate.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(
        loadErrorHandlingPolicy: LoadErrorHandlingPolicy
    ): MediaSource.Factory {
        delegate.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    private fun resolveDashManifestUriForPlayback(mediaId: String): Uri {
        val startedAt = SystemClock.elapsedRealtime()
        Log.d(TAG, "Resolving playback URL mediaId=$mediaId")
        return try {
            val result = resolveAudioResult(mediaId)
            writeStreamMetadata(mediaId, result)
            resolvePlaybackUri(result).also { uri ->
                Log.i(
                    TAG,
                    "Resolved playback URL mediaId=$mediaId host=${uri.host} " +
                        "elapsedMs=${SystemClock.elapsedRealtime() - startedAt}",
                )
            }
        } catch (error: IOException) {
            Log.w(
                TAG,
                "Playback URL resolution failed mediaId=$mediaId " +
                    "elapsedMs=${SystemClock.elapsedRealtime() - startedAt}",
                error,
            )
            throw error
        }
    }

    private fun writeStreamMetadata(mediaId: String, result: com.varshuai.a1swaara.extractor.NewPipeAudioResult) {
        val streamInfo = result.streamInfo
        val durationText = streamInfo.duration
            .takeIf { it > 0 }
            ?.let(DateUtils::formatElapsedTime)
            ?.removePrefix("0")

        dbWriteScope.launch(Dispatchers.IO) {
            val mediaItem = runCatching { findMediaItem(mediaId) }.getOrNull()
            mediaItem
                ?.mediaMetadata
                ?.extras
                ?.songBundle
                ?.takeIf { it.durationText == null }
                ?.let { extras ->
                    extras.durationText = durationText ?: return@let
                }

            runCatching {
                mediaItem?.let { item ->
                    playerRepository.insertSong(item)
                }
                playerRepository.insertFormat(
                    Format(
                        songId = mediaId,
                        itag = result.selectedItag.takeIf { it > 0 },
                        mimeType = result.selectedMimeType,
                        bitrate = result.selectedBitrate
                            .takeIf { it > 0 }
                            ?.toLong(),
                        loudnessDb = null,
                        contentLength = null,
                        lastModified = null
                    )
                )

                durationText?.let {
                    playerRepository.updateDurationText(mediaId, it)
                }
            }.onFailure {
                Log.w(TAG, "Failed to persist stream metadata for $mediaId", it)
            }
        }
    }

    private val com.varshuai.a1swaara.extractor.NewPipeAudioResult.selectedItag: Int
        get() = audioStream?.itag ?: videoStream?.itag ?: 0

    private val com.varshuai.a1swaara.extractor.NewPipeAudioResult.selectedMimeType: String?
        get() = audioStream?.format?.mimeType ?: videoStream?.format?.mimeType

    private val com.varshuai.a1swaara.extractor.NewPipeAudioResult.selectedBitrate: Int
        get() = audioStream?.averageBitrate ?: videoStream?.bitrate ?: 0

    companion object {
        private const val TAG = "NewPipeAudioMediaSourceFactory"
        private const val PRELOADED_RESULT_TTL_MS = 4 * 60 * 60 * 1000L
        private const val MAX_PRELOADED_RESULTS = 50

        private data class PreloadedAudioResult(
            val result: com.varshuai.a1swaara.extractor.NewPipeAudioResult,
            val createdAtMs: Long
        )

        private val preloadedAudioResults = object : LinkedHashMap<String, PreloadedAudioResult>(
            MAX_PRELOADED_RESULTS,
            0.75f,
            true
        ) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<String, PreloadedAudioResult>?
            ) = size > MAX_PRELOADED_RESULTS
        }

        /** Resolves the next stream early without downloading audio data. */
        fun preloadAudioResult(mediaId: String): Boolean {
            synchronized(preloadedAudioResults) {
                preloadedAudioResults[mediaId]?.takeIf { it.isFresh() }?.let { return true }
            }

            val result = NewPipeExtractorClient.preloadAudioStream(mediaId) ?: return false
            synchronized(preloadedAudioResults) {
                preloadedAudioResults[mediaId] = PreloadedAudioResult(result, System.currentTimeMillis())
            }
            return true
        }

        /** Returns direct playable audio URL if preloaded or already cached in memory. */
        fun getDirectStreamUrl(mediaId: String): String? {
            synchronized(preloadedAudioResults) {
                val cached = preloadedAudioResults[mediaId]?.takeIf { it.isFresh() }?.result
                if (cached != null) {
                    return cached.videoStream?.content ?: cached.audioStream?.content
                }
            }
            return null
        }

        fun resolveDashManifestUri(mediaId: String): Uri =
            resolvePlaybackUri(resolveAudioResult(mediaId))

        fun invalidatePreloadedAudioResult(mediaId: String) {
            synchronized(preloadedAudioResults) {
                preloadedAudioResults.remove(mediaId)
            }
        }

        fun createDataSourceFactory(
            context: Context,
            cache: Cache,
            resolveDashManifestUri: (String) -> Uri
        ): DataSource.Factory {
            val playerRepository = context.appContainer.playerRepository
            val upstreamDataSourceFactory = DefaultDataSource.Factory(
                context,
                YoutubeHttpDataSourceFactory(
                    rnParameterEnabled = true,
                    upstreamFactory = playbackHttpDataSourceFactory(context)
                )
            )

            val resolvingUpstreamDataSourceFactory = ResolvingDataSource.Factory(
                upstreamDataSourceFactory,
            ) resolver@{ dataSpec ->
                if (dataSpec.isLocal || dataSpec.uri.isNetworkStream) return@resolver dataSpec

                val mediaId = dataSpec.key
                    ?.let(PlayerService::extractYouTubeVideoId)
                    ?: PlayerService.extractYouTubeVideoId(dataSpec.uri.toString())

                dataSpec.buildUpon()
                    .setUri(resolveDashManifestUri(mediaId))
                    .setKey(mediaId)
                    .build()
            }

            val cacheSink = CacheDataSink.Factory()
                .setCache(cache)
                .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)

            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache.readOnlyWhen { PlayerPreferences.pauseCache })
                .setUpstreamDataSourceFactory(resolvingUpstreamDataSourceFactory)
                .setCacheWriteDataSinkFactory(cacheSink)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            return ConditionalCacheDataSourceFactory(
                cacheDataSourceFactory = cacheDataSourceFactory,
                upstreamDataSourceFactory = resolvingUpstreamDataSourceFactory,
                shouldCache = { dataSpec ->
                    if (dataSpec.uri.scheme == "data") return@ConditionalCacheDataSourceFactory false

                    val mediaId = dataSpec.key
                        ?.let(PlayerService::extractYouTubeVideoId)
                        ?: PlayerService.extractYouTubeVideoId(dataSpec.uri.toString())

                    // Stream through cache if active or if song is cached in any part
                    !PlayerPreferences.pauseCache || cache.isCached(mediaId, dataSpec.position, 1)
                },
            )
        }

        private fun Cache.isFullyCached(mediaId: String): Boolean {
            val contentLength = ContentMetadata.getContentLength(getContentMetadata(mediaId))
            if (contentLength <= 0L) return false
            return isCached(mediaId, 0L, contentLength)
        }

        private fun resolveAudioResult(mediaId: String): com.varshuai.a1swaara.extractor.NewPipeAudioResult =
            synchronized(preloadedAudioResults) {
                preloadedAudioResults[mediaId]?.takeIf { it.isFresh() }?.result
            } ?: resolveAudioResultUncached(mediaId).also { result ->
                synchronized(preloadedAudioResults) {
                    preloadedAudioResults[mediaId] = PreloadedAudioResult(result, System.currentTimeMillis())
                }
            }

        private fun PreloadedAudioResult.isFresh() =
            System.currentTimeMillis() - createdAtMs <= PRELOADED_RESULT_TTL_MS

        private fun resolveAudioResultUncached(mediaId: String): com.varshuai.a1swaara.extractor.NewPipeAudioResult {
            val result = try {
                NewPipeExtractorClient.resolveAudioStream(mediaId)
            } catch (error: IOException) {
                throw error
            } catch (error: ExtractionException) {
                throw IOException("NewPipe extraction failed for $mediaId", error)
            }

            if (result.streamInfo.id != mediaId) {
                throw IOException("Resolved video ID ${result.streamInfo.id} does not match requested video ID $mediaId")
            }

            return result
        }

        private fun resolvePlaybackUri(result: com.varshuai.a1swaara.extractor.NewPipeAudioResult): Uri {
            result.videoStream?.let { return validatePlaybackUri(it.content) }

            val audioStream = result.audioStream
                ?: throw IOException("No audio or muxed video stream available")

            if (!audioStream.isUrl || audioStream.deliveryMethod != DeliveryMethod.PROGRESSIVE_HTTP) {
                throw IOException(
                    "Unsupported audio stream for direct playback: " +
                        "delivery=${audioStream.deliveryMethod} isUrl=${audioStream.isUrl} itag=${audioStream.itag}"
                )
            }

            return validatePlaybackUri(audioStream.content)
        }

        private fun validatePlaybackUri(value: String): Uri = value.toUri().also { uri ->
            if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
                throw IOException("Resolved stream has an invalid network URL")
            }
        }

        private val Uri.isNetworkStream: Boolean
            get() = scheme == "http" || scheme == "https"
    }
}
