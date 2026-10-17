package com.varshuai.a1swaara.repositories

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.PlaylistPreview
import com.varshuai.a1swaara.models.SongPlaylistMap
import com.varshuai.a1swaara.transaction
import com.varshuai.a1swaara.utils.asMediaItem
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.NextBody
import app.a1swaara.providers.youtubemusic.innertube.requests.nextPage
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    suspend fun fetchSuggestions(videoId: String): List<MediaItem>?
    fun observePlaylistPreviews(sortBy: PlaylistSortBy, sortOrder: SortOrder): Flow<List<PlaylistPreview>>
    fun addQueueToPlaylist(playlist: Playlist, index: Int, mediaItems: List<MediaItem>)
}

object DatabaseQueueRepository : QueueRepository {
    override suspend fun fetchSuggestions(videoId: String): List<MediaItem>? =
        YoutubeMusicInnertube.nextPage(NextBody(videoId = videoId))
            ?.getOrNull()
            ?.itemsPage
            ?.items
            ?.map { it.asMediaItem }

    override fun observePlaylistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> = Database.playlistPreviews(sortBy = sortBy, sortOrder = sortOrder)

    override fun addQueueToPlaylist(playlist: Playlist, index: Int, mediaItems: List<MediaItem>) {
        transaction {
            val playlistId = Database.insert(playlist).takeIf { it != -1L } ?: playlist.id

            mediaItems.forEachIndexed { i, mediaItem ->
                Database.insert(mediaItem)
                Database.insert(
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = playlistId,
                        position = index + i
                    )
                )
            }
        }
    }
}
