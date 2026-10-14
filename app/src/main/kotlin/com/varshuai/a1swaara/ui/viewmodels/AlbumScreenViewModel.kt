package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.models.Album
import com.varshuai.a1swaara.repositories.AlbumRepository

class AlbumScreenViewModel(
    private val repository: AlbumRepository
) : ViewModel() {
    fun observeAlbum(browseId: String) = repository.observeAlbum(browseId)

    fun observeAlbumSongs(browseId: String) = repository.observeAlbumSongs(browseId)

    suspend fun fetchAlbumPage(browseId: String) = repository.fetchAlbumPage(browseId)

    fun replaceAlbumFromPage(
        browseId: String,
        bookmarkedAt: Long?,
        page: app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube.PlaylistOrAlbumPage
    ) = repository.replaceAlbumFromPage(
        browseId = browseId,
        bookmarkedAt = bookmarkedAt,
        page = page
    )

    fun toggleBookmark(album: Album?) {
        album ?: return
        repository.updateAlbum(
            album.copy(
                bookmarkedAt = if (album.bookmarkedAt == null) System.currentTimeMillis() else null
            )
        )
    }

    companion object {
        fun factory(repository: AlbumRepository) = viewModelFactory {
            AlbumScreenViewModel(repository = repository)
        }
    }
}
