package com.varshuai.a1swaara.ui.screens.pipedplaylist

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.HeaderPlaceholder
import com.varshuai.a1swaara.ui.components.themed.NonQueuedMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.PrimaryButton
import com.varshuai.a1swaara.ui.components.themed.SongListActionsRow
import com.varshuai.a1swaara.ui.components.themed.SongCollectionScreen
import com.varshuai.a1swaara.ui.components.themed.songCollectionItems
import com.varshuai.a1swaara.ui.components.themed.matchesSongCollectionQuery
import com.varshuai.a1swaara.ui.components.themed.adaptiveThumbnailContent
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.modifiers.songSwipeActions
import com.varshuai.a1swaara.ui.viewmodels.PipedPlaylistViewModel
import com.varshuai.a1swaara.utils.LocalPlaybackActions
import com.varshuai.a1swaara.utils.PlaylistDownloadFloatingButton
import com.varshuai.a1swaara.utils.PipedPlaylistVideoMediaItemMapper
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.enqueue
import com.varshuai.a1swaara.utils.playSongAtIndex
import com.varshuai.a1swaara.utils.rememberMediaItemsOrNull
import com.varshuai.a1swaara.utils.shufflePlay
import app.a1swaara.compose.persist.persist
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isLandscape
import app.a1swaara.providers.piped.models.Playlist
import app.a1swaara.providers.piped.models.Session
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PipedPlaylistSongList(
    session: Session,
    playlistId: UUID,
    modifier: Modifier = Modifier
) {
    val viewModel: PipedPlaylistViewModel = viewModel(
        key = "piped_playlist:$playlistId",
        factory = PipedPlaylistViewModel.factory(LocalAppContainer.current.pipedPlaylistRepository)
    )
    val (colorPalette) = LocalAppearance.current
    val menuState = LocalMenuState.current
    val playbackActions = LocalPlaybackActions.current

    var playlist by persist<Playlist>(tag = "pipedplaylist/$playlistId/playlistPage")
    var filterQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val displayedVideos = playlist?.videos.orEmpty().filter { video ->
        matchesSongCollectionQuery(filterQuery, video.title, video.uploaderName)
    }
    val mediaItems = rememberMediaItemsOrNull(displayedVideos, PipedPlaylistVideoMediaItemMapper)

    LaunchedEffect(Unit) {
        playlist = withContext(Dispatchers.IO) {
            viewModel.fetchPlaylist(
                session = session,
                playlistId = playlistId
            )
        }
    }

    val lazyListState = rememberLazyListState()

    val thumbnailContent = adaptiveThumbnailContent(
        isLoading = playlist == null,
        url = playlist?.thumbnailUrl?.toString()
    )

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
                if (playlist == null) HeaderPlaceholder(modifier = Modifier.shimmer())
                else Header(title = playlist?.name ?: stringResource(R.string.unknown)) {
                    SongListActionsRow(
                        filterQuery = filterQuery,
                        onFilterQueryChange = { filterQuery = it }
                    )
                }

                if (!isLandscape) thumbnailContent()
            }
        }
    ) {
        songCollectionItems(
            items = displayedVideos,
            isLoading = playlist == null,
        ) { index, song ->
            song.asMediaItem?.let { mediaItem ->
                SongItem(
                    song = mediaItem,
                    thumbnailSize = Dimensions.thumbnails.song,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    NonQueuedMediaItemMenu(
                                        onDismiss = menuState::hide,
                                        mediaItem = mediaItem
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
                            key = playlistId,
                            mediaItem = mediaItem
                        )
                )
            }
        }
    }
}
