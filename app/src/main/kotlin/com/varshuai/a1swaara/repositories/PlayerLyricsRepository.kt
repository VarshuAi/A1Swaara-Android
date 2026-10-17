package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Lyrics
import com.varshuai.a1swaara.transaction
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.NextBody
import app.a1swaara.providers.youtubemusic.innertube.requests.lyrics
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.kugou.KuGou
import app.a1swaara.providers.lrclib.LrcLib
import app.a1swaara.providers.lrclib.models.Track
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface PlayerLyricsRepository {
    fun observeLyrics(songId: String): Flow<Lyrics?>
    fun upsertLyrics(lyrics: Lyrics)
    suspend fun fetchYoutubeMusicInnertubeLyrics(mediaId: String): String?
    suspend fun fetchBestLrcLibLyrics(
        artist: String,
        title: String,
        duration: Duration,
        album: String?,
        synced: Boolean = true
    ): String?

    suspend fun fetchKuGouLyrics(
        artist: String,
        title: String,
        durationSeconds: Long
    ): String?

    suspend fun searchSyncedLrcLib(query: String): Result<List<Track>>?
}

object DefaultPlayerLyricsRepository : PlayerLyricsRepository {
    override fun observeLyrics(songId: String): Flow<Lyrics?> = Database.lyrics(songId)

    override fun upsertLyrics(lyrics: Lyrics) {
        transaction { Database.upsert(lyrics) }
    }

    override suspend fun fetchYoutubeMusicInnertubeLyrics(mediaId: String): String? =
        YoutubeMusicInnertube.lyrics(NextBody(videoId = mediaId))?.getOrNull()

    override suspend fun fetchBestLrcLibLyrics(
        artist: String,
        title: String,
        duration: Duration,
        album: String?,
        synced: Boolean
    ): String? = LrcLib.bestLyrics(
        artist = artist,
        title = title,
        duration = duration,
        album = album,
        synced = synced
    )?.map { it?.text }?.getOrNull()

    override suspend fun fetchKuGouLyrics(
        artist: String,
        title: String,
        durationSeconds: Long
    ): String? = KuGou.lyrics(
        artist = artist,
        title = title,
        duration = durationSeconds
    )?.map { it?.value }?.getOrNull()

    override suspend fun searchSyncedLrcLib(query: String): Result<List<Track>>? =
        LrcLib.lyrics(query = query, synced = true)
}
