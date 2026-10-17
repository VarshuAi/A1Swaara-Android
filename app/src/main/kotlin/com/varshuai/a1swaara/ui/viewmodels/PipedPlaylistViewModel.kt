package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.varshuai.a1swaara.repositories.PipedPlaylistRepository
import app.a1swaara.providers.piped.models.Playlist
import app.a1swaara.providers.piped.models.Session
import java.util.UUID

class PipedPlaylistViewModel(
    private val repository: PipedPlaylistRepository
) : ViewModel() {
    suspend fun fetchPlaylist(session: Session, playlistId: UUID): Playlist? =
        repository.fetchPlaylist(session = session, playlistId = playlistId)

    companion object {
        fun factory(repository: PipedPlaylistRepository) = viewModelFactory {
            PipedPlaylistViewModel(repository = repository)
        }
    }
}
