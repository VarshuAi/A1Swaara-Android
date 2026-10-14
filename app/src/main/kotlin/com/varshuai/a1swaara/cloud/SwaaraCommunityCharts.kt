package com.varshuai.a1swaara.cloud

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TrendingSong(
    val songId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val playCount: Long,
    val rank: Int = 0
)

/**
 * Manages crowdsourced community play tracking and trending charts across all A1 Swaara listeners.
 */
object SwaaraCommunityCharts {
    private const val TAG = "SwaaraCommunityCharts"

    private var cachedTrending: List<TrendingSong> = emptyList()
    private var lastFetchTime: Long = 0L
    private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes

    suspend fun reportSongPlay(
        songId: String,
        title: String,
        artist: String,
        artworkUrl: String?
    ) = withContext(Dispatchers.IO) {
        if (songId.isBlank()) return@withContext
        try {
            val docPath = "community_trending/$songId"
            val existing = SwaaraCloudConfig.getDocument(docPath)
            val currentPlays = (existing?.get("playCount") as? Number)?.toLong() ?: 0L
            val newPlays = currentPlays + 1L

            val data = mapOf(
                "songId" to songId,
                "title" to title,
                "artist" to artist,
                "artworkUrl" to (artworkUrl ?: ""),
                "playCount" to newPlays,
                "lastPlayed" to System.currentTimeMillis()
            )
            SwaaraCloudConfig.setDocument(docPath, data)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting play for $songId", e)
        }
    }

    suspend fun getTrendingSongs(
        limit: Int = 30,
        forceRefresh: Boolean = false
    ): List<TrendingSong> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedTrending.isNotEmpty() && (now - lastFetchTime) < CACHE_EXPIRY_MS) {
            return@withContext cachedTrending
        }

        try {
            val docs = SwaaraCloudConfig.listDocuments("community_trending", pageSize = 50)
            val songs = docs.mapNotNull { (_, fields) ->
                val songId = fields["songId"] as? String ?: return@mapNotNull null
                val title = fields["title"] as? String ?: "Track"
                val artist = fields["artist"] as? String ?: "Unknown"
                val artworkUrl = fields["artworkUrl"] as? String
                val playCount = (fields["playCount"] as? Number)?.toLong() ?: 1L
                TrendingSong(
                    songId = songId,
                    title = title,
                    artist = artist,
                    artworkUrl = artworkUrl,
                    playCount = playCount
                )
            }.sortedByDescending { it.playCount }
                .take(limit)
                .mapIndexed { index, song -> song.copy(rank = index + 1) }

            if (songs.isNotEmpty()) {
                cachedTrending = songs
                lastFetchTime = now
            }
            songs
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trending songs", e)
            cachedTrending
        }
    }
}
