package com.varshuai.a1swaara.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varshuai.a1swaara.repositories.MoodRepository
import com.varshuai.a1swaara.ui.state.LoadState
import com.varshuai.a1swaara.ui.state.launchLoad
import com.varshuai.a1swaara.utils.requireValue
import app.a1swaara.providers.youtubemusic.innertube.requests.BrowseResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoodListViewModel(
    private val repository: MoodRepository
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<LoadState<BrowseResult>>(LoadState.Idle)
    val uiState: StateFlow<LoadState<BrowseResult>> = mutableUiState.asStateFlow()
    private var loadJob: Job? = null

    fun load(
        browseId: String,
        params: String?,
        cachedPage: BrowseResult? = null,
        force: Boolean = false,
    ) {
        if (!force && mutableUiState.value is LoadState.Content) return
        if (!force && cachedPage != null) {
            mutableUiState.value = LoadState.Content(cachedPage)
            return
        }
        if (loadJob?.isActive == true) return

        loadJob = mutableUiState.launchLoad(viewModelScope) {
                repository.fetchMoodPage(browseId, params).requireValue(
                    nullResultMessage = "Mood request was not executed",
                    nullValueMessage = "Mood response was empty",
                ).getOrThrow()
        }
    }

    companion object {
        fun factory(repository: MoodRepository) = viewModelFactory {
            MoodListViewModel(repository = repository)
        }
    }
}
