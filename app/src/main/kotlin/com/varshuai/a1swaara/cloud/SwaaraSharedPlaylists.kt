package com.varshuai.a1swaara.cloud

import android.content.Context
import android.util.Log
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.models.SongPlaylistMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.SecureRandom

data class SharedPlaylistData(
    val code: String,
    val name: String,
    val author: String,
    val songCount: Int,
    val createdAt: Long,
    val songs: List<SharedSongItem>
)

data class SharedSongItem(
    val id: String,
    val title: String,
    val artistsText: String?,
    val durationText: String?,
    val thumbnailUrl: String?
)

data class PublishResult(
    val success: Boolean,
    val code: String? = null,
    val shareUrl: String? = null,
    val message: String
)

/**
 * Manages public, shareable playlists with lightweight 6-character codes (SWxxxx).
 */
object SwaaraSharedPlaylists {
    private const val TAG = "SwaaraSharedPlaylists"
    private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excludes ambiguous I, O, 0, 1
    private val random = SecureRandom()

    fun generateShareCode(): String {
        val sb = StringBuilder("SW")
        for (i in 0 until 4) {
            sb.append(CHARS[random.nextInt(CHARS.length)])
        }
        return sb.toString()
    }

    suspend fun publishPlaylist(
        context: Context,
        playlistId: Long
    ): PublishResult = withContext(Dispatchers.IO) {
        try {
            val preview = Database.playlist(playlistId).first()
                ?: return@withContext PublishResult(false, message = "Playlist not found locally")
            val songs = Database.playlistSongs(playlistId).first()
            if (songs.isEmpty()) {
                return@withContext PublishResult(false, message = "Playlist is empty")
            }

            val auth = SwaaraCloudAuth.getInstance(context)
            val code = generateShareCode()
            val timestamp = System.currentTimeMillis()

            val songsData = songs.map { s ->
                mapOf(
                    "id" to s.id,
                    "title" to s.title,
                    "artistsText" to (s.artistsText ?: ""),
                    "durationText" to (s.durationText ?: ""),
                    "thumbnailUrl" to (s.thumbnailUrl ?: "")
                )
            }

            val docData = mapOf(
                "code" to code,
                "name" to preview.name,
                "author" to auth.displayName,
                "songCount" to songs.size,
                "createdAt" to timestamp,
                "songs" to songsData
            )

            val success = SwaaraCloudConfig.setDocument("shared_playlists/$code", docData)
            if (success) {
                val shareUrl = "https://a1swaara.vercel.app/p/$code"
                PublishResult(
                    success = true,
                    code = code,
                    shareUrl = shareUrl,
                    message = "Playlist shared successfully! Code: $code"
                )
            } else {
                PublishResult(false, message = "Failed to upload playlist to cloud")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing playlist", e)
            PublishResult(false, message = "Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun fetchPlaylist(code: String): SharedPlaylistData? = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length < 4) return@withContext null

        try {
            val doc = SwaaraCloudConfig.getDocument("shared_playlists/$cleanCode") ?: return@withContext null
            val name = doc["name"] as? String ?: "Shared Playlist"
            val author = doc["author"] as? String ?: "Swaara User"
            val songCount = (doc["songCount"] as? Number)?.toInt() ?: 0
            val createdAt = (doc["createdAt"] as? Number)?.toLong() ?: 0L

            @Suppress("UNCHECKED_CAST")
            val songsRaw = doc["songs"] as? List<Map<String, Any?>> ?: emptyList()
            val songItems = songsRaw.map { s ->
                SharedSongItem(
                    id = s["id"] as? String ?: "",
                    title = s["title"] as? String ?: "Track",
                    artistsText = s["artistsText"] as? String,
                    durationText = s["durationText"] as? String,
                    thumbnailUrl = s["thumbnailUrl"] as? String
                )
            }.filter { it.id.isNotBlank() }

            SharedPlaylistData(
                code = cleanCode,
                name = name,
                author = author,
                songCount = songCount.coerceAtLeast(songItems.size),
                createdAt = createdAt,
                songs = songItems
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching playlist $cleanCode", e)
            null
        }
    }

    suspend fun importPlaylist(shared: SharedPlaylistData): Long = withContext(Dispatchers.IO) {
        val existingPreviews = Database.playlistPreviewsByNameAsc().first()
        var targetName = shared.name
        var counter = 1
        while (existingPreviews.any { it.playlist.name == targetName }) {
            targetName = "${shared.name} ($counter)"
            counter++
        }

        val newPlaylistId = Database.insert(Playlist(name = targetName))
        shared.songs.forEachIndexed { index, item ->
            val song = Song(
                id = item.id,
                title = item.title,
                artistsText = item.artistsText,
                durationText = item.durationText,
                thumbnailUrl = item.thumbnailUrl
            )
            Database.insert(song)
            Database.insert(
                SongPlaylistMap(
                    songId = item.id,
                    playlistId = newPlaylistId,
                    position = index
                )
            )
        }
        newPlaylistId
    }
}
