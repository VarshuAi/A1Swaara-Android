package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.repositories.MediaItemMenuRepository
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder

class MediaItemMenuViewModel(
    private val repository: MediaItemMenuRepository
) : ViewModel() {
    fun removeFromPlaylist(playlistId: Long, positionInPlaylist: Int, songId: String) =
        repository.removeFromPlaylist(
            playlistId = playlistId,
            positionInPlaylist = positionInPlaylist,
            songId = songId
        )

    fun addToPlaylist(mediaItem: MediaItem, playlist: Playlist, position: Int) =
        repository.addToPlaylist(mediaItem = mediaItem, playlist = playlist, position = position)

    suspend fun getSongAlbumInfo(mediaId: String) = repository.getSongAlbumInfo(mediaId)

    suspend fun getSongArtistInfo(mediaId: String) = repository.getSongArtistInfo(mediaId)

    fun observeLikedAt(mediaId: String) = repository.observeLikedAt(mediaId)

    fun toggleLike(mediaItem: MediaItem, likedAt: Long?) =
        repository.toggleLike(mediaItem = mediaItem, likedAt = likedAt)

    fun observeBlacklisted(mediaId: String) = repository.observeBlacklisted(mediaId)

    fun toggleBlacklist(mediaItem: MediaItem) = repository.toggleBlacklist(mediaItem)

    fun observePlaylistPreviews(sortBy: PlaylistSortBy, sortOrder: SortOrder) =
        repository.observePlaylistPreviews(sortBy = sortBy, sortOrder = sortOrder)

    companion object {
        fun factory(repository: MediaItemMenuRepository) = viewModelFactory {
            MediaItemMenuViewModel(repository = repository)
        }
    }
}
