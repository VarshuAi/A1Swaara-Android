package com.varshuai.a1swaara.ui.screens.artist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.rememberSongListState
import com.varshuai.a1swaara.ui.components.themed.HideSongDialog
import com.varshuai.a1swaara.ui.components.themed.NonQueuedMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.PrimaryButton
import com.varshuai.a1swaara.ui.components.themed.SongCollectionScreen
import com.varshuai.a1swaara.ui.components.themed.songCollectionItems
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.modifiers.songSwipeActions
import com.varshuai.a1swaara.ui.viewmodels.ArtistLocalSongsViewModel
import com.varshuai.a1swaara.utils.LocalPlaybackActions
import com.varshuai.a1swaara.utils.PlaylistDownloadFloatingButton
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.rememberMediaItems
import app.a1swaara.compose.persist.persist
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isLandscape
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistLocalSongs(
    browseId: String,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ArtistLocalSongsViewModel = viewModel(
        key = "artist_local_songs:$browseId",
        factory = ArtistLocalSongsViewModel.factory(
            browseId = browseId,
            repository = LocalAppContainer.current.libraryRepository
        )
    )
    val playbackActions = LocalPlaybackActions.current
    val (colorPalette) = LocalAppearance.current
    val menuState = LocalMenuState.current

    var songs by persist<List<Song>?>("artist/$browseId/localSongs")
    var hidingSong by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.observeSongs().collect { songs = it }
    }

    val lazyListState = rememberLazyListState()
    val listState = rememberSongListState(songs, isLoading = songs == null)
    val mediaItems = if (listState.items.isNotEmpty()) rememberMediaItems(listState.items) else null

    SongCollectionScreen(
        thumbnailContent = thumbnailContent,
        modifier = modifier,
        listState = lazyListState,
        listBackground = colorPalette.background0,
        onShuffle = { mediaItems?.let(playbackActions::shufflePlay) },
        floatingActionsContent = {
            mediaItems?.let { items ->
                PlaylistDownloadFloatingButton(items.toImmutableList())
                PrimaryButton(
                    icon = R.drawable.enqueue,
                    onClick = { playbackActions.enqueue(items) }
                )
            }
        },
        headerContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                headerContent(null)

                if (!isLandscape) thumbnailContent()
            }
        }
    ) {
        songCollectionItems(
            items = listState.items,
            isLoading = listState.isLoading,
            key = { _, song -> song.id },
        ) { index, song ->
                if (hidingSong == song.id) HideSongDialog(
                    song = song,
                    onDismiss = { hidingSong = null },
                    onConfirm = { hidingSong = null }
                )

                SongItem(
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    NonQueuedMediaItemMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = song.asMediaItem
                                    )
                                }
                            },
                            onClick = {
                                mediaItems?.let { items ->
                                    playbackActions.playAtIndex(items, index)
                                }
                            }
                        )
                        .songSwipeActions(
                            key = listState.items,
                            mediaItem = song.asMediaItem,
                            songToHide = song,
                            onSwipeLeftRequested = { hidingSong = it.id }
                        ),
                    song = song,
                    thumbnailSize = Dimensions.thumbnails.song
                )
        }
    }
}
