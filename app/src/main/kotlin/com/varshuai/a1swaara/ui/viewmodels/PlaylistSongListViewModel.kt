package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.repositories.PlaylistRepository
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube

class PlaylistSongListViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {
    suspend fun fetchPlaylistPage(
        browseId: String,
        params: String?,
        maxDepth: Int?,
        shouldDedup: Boolean
    ): YoutubeMusicInnertube.PlaylistOrAlbumPage? = repository.fetchPlaylistPage(
        browseId = browseId,
        params = params,
        maxDepth = maxDepth,
        shouldDedup = shouldDedup
    )

    fun importPlaylist(
        name: String,
        browseId: String,
        thumbnailUrl: String?,
        songs: List<YoutubeMusicInnertube.SongItem>?
    ) = repository.importPlaylist(
        name = name,
        browseId = browseId,
        thumbnailUrl = thumbnailUrl,
        songs = songs
    )

    companion object {
        fun factory(repository: PlaylistRepository) = viewModelFactory {
            PlaylistSongListViewModel(repository = repository)
        }
    }
}
