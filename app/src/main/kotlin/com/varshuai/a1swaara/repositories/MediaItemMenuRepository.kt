package com.varshuai.a1swaara.repositories

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Info
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.PlaylistPreview
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.models.SongPlaylistMap
import com.varshuai.a1swaara.query
import com.varshuai.a1swaara.transaction
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface MediaItemMenuRepository {
    fun removeFromPlaylist(playlistId: Long, positionInPlaylist: Int, songId: String)
    fun addToPlaylist(mediaItem: MediaItem, playlist: Playlist, position: Int)
    suspend fun getSongAlbumInfo(mediaId: String): Info?
    suspend fun getSongArtistInfo(mediaId: String): List<Info>?
    fun observeLikedAt(mediaId: String): Flow<Long?>
    fun toggleLike(mediaItem: MediaItem, likedAt: Long?)
    fun observeBlacklisted(mediaId: String): Flow<Boolean>
    fun toggleBlacklist(mediaItem: MediaItem)
    fun observePlaylistPreviews(sortBy: PlaylistSortBy, sortOrder: SortOrder): Flow<List<PlaylistPreview>>
}

object DatabaseMediaItemMenuRepository : MediaItemMenuRepository {
    override fun removeFromPlaylist(playlistId: Long, positionInPlaylist: Int, songId: String) {
        transaction {
            Database.move(playlistId, positionInPlaylist, Int.MAX_VALUE)
            Database.delete(SongPlaylistMap(songId, playlistId, Int.MAX_VALUE))
        }
    }

    override fun addToPlaylist(mediaItem: MediaItem, playlist: Playlist, position: Int) {
        transaction {
            Database.insert(mediaItem)
            Database.insert(
                SongPlaylistMap(
                    songId = mediaItem.mediaId,
                    playlistId = Database.insert(playlist).takeIf { it != -1L } ?: playlist.id,
                    position = position
                )
            )
        }
    }

    override suspend fun getSongAlbumInfo(mediaId: String): Info? = Database.songAlbumInfo(mediaId).firstOrNull()

    override suspend fun getSongArtistInfo(mediaId: String): List<Info>? = Database.songArtistInfo(mediaId)

    override fun observeLikedAt(mediaId: String): Flow<Long?> = Database.likedAt(mediaId)

    override fun toggleLike(mediaItem: MediaItem, likedAt: Long?) {
        query {
            if (
                Database.like(
                    songId = mediaItem.mediaId,
                    likedAt = if (likedAt == null) System.currentTimeMillis() else null
                ) != 0
            ) return@query

            Database.insert(mediaItem, Song::toggleLike)
        }
    }

    override fun observeBlacklisted(mediaId: String): Flow<Boolean> = Database.blacklisted(mediaId)

    override fun toggleBlacklist(mediaItem: MediaItem) {
        transaction {
            Database.insert(mediaItem)
            Database.toggleBlacklist(mediaItem.mediaId)
        }
    }

    override fun observePlaylistPreviews(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ): Flow<List<PlaylistPreview>> = Database.playlistPreviews(sortBy = sortBy, sortOrder = sortOrder)
}
