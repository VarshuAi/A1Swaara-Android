package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.repositories.LibraryRepository
import app.a1swaara.core.data.enums.AlbumSortBy
import app.a1swaara.core.data.enums.SortOrder

class HomeAlbumsViewModel(
    private val repository: LibraryRepository
) : ViewModel() {
    fun observeAlbums(sortBy: AlbumSortBy, sortOrder: SortOrder) =
        repository.observeAlbums(sortBy = sortBy, sortOrder = sortOrder)

    companion object {
        fun factory(repository: LibraryRepository) = viewModelFactory {
            HomeAlbumsViewModel(repository = repository)
        }
    }
}
