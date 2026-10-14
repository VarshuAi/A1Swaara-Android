package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.repositories.HomeRepository
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder

class HomePlaylistsViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    fun observePlaylistPreviews(sortBy: PlaylistSortBy, sortOrder: SortOrder) =
        repository.observePlaylistPreviews(sortBy = sortBy, sortOrder = sortOrder)

    fun observePipedPlaylists() = repository.observePipedPlaylists()

    fun createPlaylist(name: String) = repository.createPlaylist(name)

    companion object {
        fun factory(repository: HomeRepository) = viewModelFactory {
            HomePlaylistsViewModel(repository = repository)
        }
    }
}
