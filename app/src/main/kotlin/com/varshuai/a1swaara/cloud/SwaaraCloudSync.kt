package com.varshuai.a1swaara.cloud

import android.content.Context
import android.util.Log
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.models.SongPlaylistMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SyncResult(
    val success: Boolean,
    val favoritesCount: Int = 0,
    val playlistsCount: Int = 0,
    val message: String
)

/**
 * High-performance cloud backup and synchronization engine for A1 Swaara.
 * Connects the local Room database to Firebase Firestore.
 */
object SwaaraCloudSync {
    private const val TAG = "SwaaraCloudSync"

    suspend fun backup(context: Context): SyncResult = withContext(Dispatchers.IO) {
        val auth = SwaaraCloudAuth.getInstance(context)
        val userId = auth.userId

        try {
            // 1. Fetch local favorites
            val favorites = Database.favoritesByLikedAtDesc().first()
            val favoritesList = favorites.map { song ->
                mapOf(
                    "id" to song.id,
                    "title" to song.title,
                    "artistsText" to (song.artistsText ?: ""),
                    "durationText" to (song.durationText ?: ""),
                    "thumbnailUrl" to (song.thumbnailUrl ?: ""),
                    "likedAt" to (song.likedAt ?: System.currentTimeMillis())
                )
            }

            // 2. Fetch local playlists and their songs
            val previews = Database.playlistPreviewsByNameAsc().first()
            val playlistsData = mutableListOf<Map<String, Any?>>()

            for (preview in previews) {
                val songs = Database.playlistSongs(preview.playlist.id).first()
                val songsList = songs.map { s ->
                    mapOf(
                        "id" to s.id,
                        "title" to s.title,
                        "artistsText" to (s.artistsText ?: ""),
                        "durationText" to (s.durationText ?: ""),
                        "thumbnailUrl" to (s.thumbnailUrl ?: "")
                    )
                }
                playlistsData.add(
                    mapOf(
                        "name" to preview.playlist.name,
                        "songs" to songsList
                    )
                )
            }

            val timestamp = System.currentTimeMillis()

            // 3. Write favorites to cloud
            val favDocPath = "users/$userId/library/favorites"
            val favSuccess = SwaaraCloudConfig.setDocument(
                favDocPath,
                mapOf(
                    "songs" to favoritesList,
                    "count" to favoritesList.size,
                    "updatedAt" to timestamp
                )
            )

            // 4. Write playlists to cloud
            val plDocPath = "users/$userId/library/playlists"
            val plSuccess = SwaaraCloudConfig.setDocument(
                plDocPath,
                mapOf(
                    "playlists" to playlistsData,
                    "count" to playlistsData.size,
                    "updatedAt" to timestamp
                )
            )

            // 5. Write user metadata
            val userDocPath = "users/$userId"
            SwaaraCloudConfig.setDocument(
                userDocPath,
                mapOf(
                    "userId" to userId,
                    "displayName" to auth.displayName,
                    "lastBackup" to timestamp,
                    "favoritesCount" to favoritesList.size,
                    "playlistsCount" to playlistsData.size
                )
            )

            if (favSuccess && plSuccess) {
                auth.lastSyncTimestamp = timestamp
                SyncResult(
                    success = true,
                    favoritesCount = favoritesList.size,
                    playlistsCount = playlistsData.size,
                    message = "Successfully backed up ${favoritesList.size} favorites and ${playlistsData.size} playlists to cloud"
                )
            } else {
                SyncResult(
                    success = false,
                    message = "Cloud backup failed. Please check network connection."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup exception", e)
            SyncResult(
                success = false,
                message = "Backup error: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    suspend fun restore(context: Context): SyncResult = withContext(Dispatchers.IO) {
        val auth = SwaaraCloudAuth.getInstance(context)
        val userId = auth.userId

        try {
            var restoredFavorites = 0
            var restoredPlaylists = 0

            // 1. Restore favorites
            val favDoc = SwaaraCloudConfig.getDocument("users/$userId/library/favorites")
            if (favDoc != null) {
                @Suppress("UNCHECKED_CAST")
                val songsList = favDoc["songs"] as? List<Map<String, Any?>> ?: emptyList()
                for (item in songsList) {
                    val id = item["id"] as? String ?: continue
                    val title = item["title"] as? String ?: "Track"
                    val artistsText = item["artistsText"] as? String
                    val durationText = item["durationText"] as? String
                    val thumbnailUrl = item["thumbnailUrl"] as? String
                    val likedAt = (item["likedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

                    val song = Song(
                        id = id,
                        title = title,
                        artistsText = artistsText,
                        durationText = durationText,
                        thumbnailUrl = thumbnailUrl,
                        likedAt = likedAt
                    )
                    Database.insert(song)
                    Database.like(id, likedAt)
                    restoredFavorites++
                }
            }

            // 2. Restore playlists
            val plDoc = SwaaraCloudConfig.getDocument("users/$userId/library/playlists")
            if (plDoc != null) {
                @Suppress("UNCHECKED_CAST")
                val playlistsList = plDoc["playlists"] as? List<Map<String, Any?>> ?: emptyList()
                val existingPreviews = Database.playlistPreviewsByNameAsc().first()
                val existingNames = existingPreviews.map { it.playlist.name }.toSet()

                for (plItem in playlistsList) {
                    val plName = plItem["name"] as? String ?: continue
                    @Suppress("UNCHECKED_CAST")
                    val songs = plItem["songs"] as? List<Map<String, Any?>> ?: emptyList()

                    val targetPlaylistId = if (plName in existingNames) {
                        existingPreviews.first { it.playlist.name == plName }.playlist.id
                    } else {
                        Database.insert(Playlist(name = plName))
                    }

                    songs.forEachIndexed { index, sItem ->
                        val id = sItem["id"] as? String ?: return@forEachIndexed
                        val title = sItem["title"] as? String ?: "Track"
                        val artistsText = sItem["artistsText"] as? String
                        val durationText = sItem["durationText"] as? String
                        val thumbnailUrl = sItem["thumbnailUrl"] as? String

                        val song = Song(
                            id = id,
                            title = title,
                            artistsText = artistsText,
                            durationText = durationText,
                            thumbnailUrl = thumbnailUrl
                        )
                        Database.insert(song)
                        Database.insert(
                            SongPlaylistMap(
                                songId = id,
                                playlistId = targetPlaylistId,
                                position = index
                            )
                        )
                    }
                    restoredPlaylists++
                }
            }

            auth.lastSyncTimestamp = System.currentTimeMillis()

            SyncResult(
                success = true,
                favoritesCount = restoredFavorites,
                playlistsCount = restoredPlaylists,
                message = "Restored $restoredFavorites favorites and $restoredPlaylists playlists from cloud"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Restore exception", e)
            SyncResult(
                success = false,
                message = "Restore error: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    private var autoBackupJob: kotlinx.coroutines.Job? = null
    private val syncScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    /**
     * Debounced automatic cloud backup triggered on library changes.
     * Backs up to Firebase if auto-sync is enabled in SwaaraCloudAuth.
     */
    fun scheduleAutoBackup(context: Context, delayMs: Long = 30000L) {
        val auth = SwaaraCloudAuth.getInstance(context)
        if (!auth.isAutoSyncEnabled) return

        autoBackupJob?.cancel()
        autoBackupJob = syncScope.launch {
            kotlinx.coroutines.delay(delayMs)
            Log.d(TAG, "Executing scheduled background auto-backup to cloud...")
            backup(context.applicationContext)
        }
    }
}

