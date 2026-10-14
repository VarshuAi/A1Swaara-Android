package com.varshuai.a1swaara.ui.screens.playlist

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.HeaderIconButton
import com.varshuai.a1swaara.ui.components.themed.HeaderPlaceholder
import com.varshuai.a1swaara.ui.components.themed.NonQueuedMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.PlaylistInfo
import com.varshuai.a1swaara.ui.components.themed.PrimaryButton
import com.varshuai.a1swaara.ui.components.themed.SongListActionsRow
import com.varshuai.a1swaara.ui.components.themed.SongCollectionScreen
import com.varshuai.a1swaara.ui.components.themed.songCollectionItems
import com.varshuai.a1swaara.ui.components.themed.matchesSongCollectionQuery
import com.varshuai.a1swaara.ui.components.themed.TextFieldDialog
import com.varshuai.a1swaara.ui.components.themed.adaptiveThumbnailContent
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.modifiers.songSwipeActions
import com.varshuai.a1swaara.ui.viewmodels.PlaylistSongListViewModel
import com.varshuai.a1swaara.utils.LocalPlaybackActions
import com.varshuai.a1swaara.utils.PlaylistDownloadFloatingButton
import com.varshuai.a1swaara.utils.YoutubeMusicInnertubeSongMediaItemMapper
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.rememberMediaItemsOrNull
import app.a1swaara.compose.persist.persist
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isLandscape
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistSongList(
    browseId: String,
    params: String?,
    maxDepth: Int?,
    shouldDedup: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel: PlaylistSongListViewModel = viewModel(
        key = "playlist:$browseId",
        factory = PlaylistSongListViewModel.factory(LocalAppContainer.current.playlistRepository)
    )
    val (colorPalette) = LocalAppearance.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playbackActions = LocalPlaybackActions.current

    // v6 invalidates partial pages that were previously treated as complete
    // when a continuation request failed.
    var playlistPage by persist<YoutubeMusicInnertube.PlaylistOrAlbumPage?>("playlist/$browseId/playlistPage/v6")

    LaunchedEffect(Unit) {
        if (playlistPage != null && playlistPage?.songsPage?.continuation == null) return@LaunchedEffect

        playlistPage = withContext(Dispatchers.IO) {
            viewModel.fetchPlaylistPage(
                browseId = browseId,
                params = params,
                maxDepth = maxDepth,
                shouldDedup = shouldDedup
            )
        }
    }

    var isImportingPlaylist by rememberSaveable { mutableStateOf(false) }

    if (isImportingPlaylist) TextFieldDialog(
        hintText = stringResource(R.string.enter_playlist_name_prompt),
        initialTextInput = playlistPage?.title.orEmpty(),
        onDismiss = { isImportingPlaylist = false },
        onAccept = { text ->
            viewModel.importPlaylist(
                name = text,
                browseId = browseId,
                thumbnailUrl = playlistPage?.thumbnail?.url,
                songs = playlistPage?.songsPage?.items
            )
        }
    )

    val playlistItems = playlistPage?.songsPage?.items
    var filterQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val displayedPlaylistItems = playlistItems.orEmpty().filter { song ->
        matchesSongCollectionQuery(
            filterQuery,
            song.info?.name,
            song.authors?.joinToString { it.name.orEmpty() }
        )
    }
    val mediaItems = rememberMediaItemsOrNull(
        displayedPlaylistItems,
        YoutubeMusicInnertubeSongMediaItemMapper
    )

    val headerContent: @Composable () -> Unit = {
        if (playlistPage == null) HeaderPlaceholder(modifier = Modifier.shimmer())
        else Header(title = playlistPage?.title ?: stringResource(R.string.unknown)) {
            SongListActionsRow(
                filterQuery = filterQuery,
                onFilterQueryChange = { filterQuery = it },
                trailingContent = {
                    HeaderIconButton(
                        icon = R.drawable.add,
                        color = colorPalette.text,
                        onClick = { isImportingPlaylist = true }
                    )

                    HeaderIconButton(
                        icon = R.drawable.share_social,
                        color = colorPalette.text,
                        onClick = {
                            (
                                    playlistPage?.url
                                        ?: "https://music.youtube.com/playlist?list=${
                                            browseId.removePrefix(
                                                "VL"
                                            )
                                        }"
                                    ).let { url ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }

                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                        }
                    )
                }
            )
        }
    }

    val thumbnailContent = adaptiveThumbnailContent(
        isLoading = playlistPage == null,
        url = playlistPage?.thumbnail?.url
    )

    val lazyListState = rememberLazyListState()

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
                headerContent()
                if (!isLandscape) thumbnailContent()
                PlaylistInfo(playlist = playlistPage)
            }
        }
    ) {
        songCollectionItems(
            items = displayedPlaylistItems,
            isLoading = playlistPage == null,
        ) { index, song ->
            SongItem(
                song = song,
                thumbnailSize = Dimensions.thumbnails.song,
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
                        key = displayedPlaylistItems,
                        mediaItem = song.asMediaItem
                    )
            )
        }
    }
}
