package com.varshuai.a1swaara.ui.screens.localplaylist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.ui.components.themed.Scaffold
import com.varshuai.a1swaara.ui.components.themed.adaptiveThumbnailContent
import com.varshuai.a1swaara.ui.screens.GlobalRoutes
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.ui.viewmodels.LocalPlaylistViewModel
import app.a1swaara.compose.persist.PersistMapCleanup
import app.a1swaara.compose.persist.persist
import app.a1swaara.compose.persist.persistList
import app.a1swaara.compose.routing.RouteHandler
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filterNotNull

@Route
@Composable
fun LocalPlaylistScreen(playlistId: Long) {
    val viewModel: LocalPlaylistViewModel = viewModel(
        key = "local_playlist:$playlistId",
        factory = LocalPlaylistViewModel.factory(LocalAppContainer.current.localPlaylistRepository)
    )
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "localPlaylist/$playlistId/")

    RouteHandler {
        GlobalRoutes()

        Content {
            var playlist by persist<Playlist?>("localPlaylist/$playlistId/playlist")
            var songs by persistList<Song>("localPlaylist/$playlistId/songs")

            LaunchedEffect(Unit) {
                viewModel
                    .observePlaylist(playlistId)
                    .filterNotNull()
                    .collect { playlist = it }
            }

            LaunchedEffect(Unit) {
                viewModel
                    .observePlaylistSongs(playlistId)
                    .collect { songs = it.toImmutableList() }
            }

            val thumbnailContent = remember(playlist) {
                playlist?.thumbnail?.let { url ->
                    adaptiveThumbnailContent(
                        isLoading = false,
                        url = url
                    )
                } ?: { }
            }

            Scaffold(
                key = "localplaylist",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChange = { },
                tabColumnContent = {
                    tab(0, R.string.songs, R.drawable.musical_notes)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    playlist?.let {
                        when (currentTabIndex) {
                            0 -> LocalPlaylistSongs(
                                viewModel = viewModel,
                                playlist = it,
                                songs = songs,
                                thumbnailContent = thumbnailContent,
                                onDelete = pop
                            )
                        }
                    }
                }
            }
        }
    }
}
