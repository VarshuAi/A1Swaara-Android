package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.repositories.QueueRepository
import com.varshuai.a1swaara.ui.state.LoadState
import com.varshuai.a1swaara.ui.state.launchLoad
import app.a1swaara.core.data.enums.PlaylistSortBy
import app.a1swaara.core.data.enums.SortOrder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: QueueRepository
) : ViewModel() {
    private val mutableSuggestions = MutableStateFlow<LoadState<List<MediaItem>>>(LoadState.Idle)
    val suggestions: StateFlow<LoadState<List<MediaItem>>> = mutableSuggestions.asStateFlow()
    private var suggestionsJob: Job? = null

    fun loadSuggestions(videoId: String) {
        suggestionsJob?.cancel()
        suggestionsJob = mutableSuggestions.launchLoad(viewModelScope) {
                requireNotNull(repository.fetchSuggestions(videoId)) {
                    "Queue suggestions response was empty"
                }
        }
    }

    fun clearSuggestions() {
        suggestionsJob?.cancel()
        mutableSuggestions.value = LoadState.Idle
    }

    fun observePlaylistPreviews(sortBy: PlaylistSortBy, sortOrder: SortOrder) =
        repository.observePlaylistPreviews(sortBy = sortBy, sortOrder = sortOrder)

    fun addQueueToPlaylist(playlist: Playlist, index: Int, mediaItems: List<MediaItem>) =
        repository.addQueueToPlaylist(playlist = playlist, index = index, mediaItems = mediaItems)

    companion object {
        fun factory(repository: QueueRepository) = viewModelFactory {
            QueueViewModel(repository = repository)
        }
    }
}
