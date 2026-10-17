package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.repositories.LibraryRepository

class ArtistLocalSongsViewModel(
    private val repository: LibraryRepository,
    private val browseId: String
) : ViewModel() {
    fun observeSongs() = repository.observeArtistSongs(artistId = browseId)

    companion object {
        fun factory(
            browseId: String,
            repository: LibraryRepository
        ) = viewModelFactory {
            ArtistLocalSongsViewModel(repository = repository, browseId = browseId)
        }
    }
}
