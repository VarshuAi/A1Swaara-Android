package com.varshuai.a1swaara.ui.screens.localplaylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.themed.CircularProgressIndicator
import com.varshuai.a1swaara.ui.components.themed.ConfirmationDialog
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.HeaderIconButton
import com.varshuai.a1swaara.ui.components.themed.HideSongDialog
import com.varshuai.a1swaara.ui.components.themed.InPlaylistMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.Menu
import com.varshuai.a1swaara.ui.components.themed.MenuEntry
import com.varshuai.a1swaara.ui.components.themed.PrimaryButton
import com.varshuai.a1swaara.ui.components.themed.ReorderHandle
import com.varshuai.a1swaara.ui.components.themed.SongListActionsRow
import com.varshuai.a1swaara.ui.components.themed.SongCollectionScreen
import com.varshuai.a1swaara.ui.components.themed.songCollectionItems
import com.varshuai.a1swaara.ui.components.themed.matchesSongCollectionQuery
import com.varshuai.a1swaara.ui.components.themed.TextFieldDialog
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.items.SongTotalPlayTimeOverlay
import com.varshuai.a1swaara.ui.modifiers.songSwipeActions
import com.varshuai.a1swaara.ui.screens.home.HeaderSongSortBy
import com.varshuai.a1swaara.ui.viewmodels.LocalPlaylistViewModel
import com.varshuai.a1swaara.utils.LocalPlaybackActions
import com.varshuai.a1swaara.utils.PlaylistDownloadFloatingButton
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.enqueue
import com.varshuai.a1swaara.utils.launchYouTubeMusic
import com.varshuai.a1swaara.utils.playSongAtIndex
import com.varshuai.a1swaara.utils.rememberMediaItems
import com.varshuai.a1swaara.utils.shufflePlay
import com.varshuai.a1swaara.utils.toast
import com.varshuai.a1swaara.cloud.SwaaraSharedPlaylists
import app.a1swaara.compose.reordering.animateItemPlacement
import app.a1swaara.compose.reordering.draggedItem
import app.a1swaara.compose.reordering.rememberReorderingState
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.enumSaver
import app.a1swaara.core.ui.utils.isLandscape
import app.a1swaara.core.data.enums.SongSortBy
import app.a1swaara.core.data.enums.SortOrder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlaylistSongs(
    viewModel: LocalPlaylistViewModel,
    playlist: Playlist,
    songs: ImmutableList<Song>,
    onDelete: () -> Unit,
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val playbackActions = LocalPlaybackActions.current
    val menuState = LocalMenuState.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val youtubeMusicNotInstalled = stringResource(R.string.youtube_music_not_installed)

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    var filterQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var sortBy by rememberSaveable(stateSaver = enumSaver()) { mutableStateOf(SongSortBy.DateAdded) }
    var sortOrder by rememberSaveable(stateSaver = enumSaver()) { mutableStateOf(SortOrder.Descending) }
    val displayedSongs = songs.filter {
        matchesSongCollectionQuery(filterQuery, it.title, it.artistsText)
    }.let { filteredSongs ->
        when (sortBy) {
            SongSortBy.DateAdded -> if (sortOrder == SortOrder.Descending) {
                filteredSongs
            } else {
                filteredSongs.asReversed()
            }

            SongSortBy.PlayTime -> filteredSongs.sortedBy(Song::totalPlayTimeMs).let {
                if (sortOrder == SortOrder.Ascending) it else it.asReversed()
            }

            SongSortBy.Title -> filteredSongs.sortedBy(Song::title).let {
                if (sortOrder == SortOrder.Ascending) it else it.asReversed()
            }
        }
    }
    val canReorder = filterQuery.isNullOrBlank() &&
        sortBy == SongSortBy.DateAdded && sortOrder == SortOrder.Descending
    val mediaItems = rememberMediaItems(displayedSongs)

    var loading by remember { mutableStateOf(false) }
    var hidingSong by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (DataPreferences.autoSyncPlaylists) playlist.browseId?.let { browseId ->
            loading = true
            viewModel.sync(playlist, browseId)
            loading = false
        }
    }

    val reorderingState = rememberReorderingState(
        lazyListState = lazyListState,
        key = songs,
        onDragEnd = { fromIndex, toIndex ->
            viewModel.move(playlist.id, fromIndex, toIndex)
        },
        extraItemCount = 1
    )

    var isRenaming by rememberSaveable { mutableStateOf(false) }

    if (isRenaming) TextFieldDialog(
        hintText = stringResource(R.string.enter_playlist_name_prompt),
        initialTextInput = playlist.name,
        onDismiss = { isRenaming = false },
        onAccept = { text ->
            viewModel.rename(playlist = playlist, name = text)
        }
    )

    var isDeleting by rememberSaveable { mutableStateOf(false) }

    if (isDeleting) ConfirmationDialog(
        text = stringResource(R.string.confirm_delete_playlist),
        onDismiss = { isDeleting = false },
        onConfirm = {
            viewModel.delete(playlist)
            onDelete()
        }
    )

    LookaheadScope {
        SongCollectionScreen(
            thumbnailContent = thumbnailContent,
            modifier = modifier,
            listState = reorderingState.lazyListState,
            listBackground = colorPalette.background0,
            shuffleVisible = !reorderingState.isDragging,
            onShuffle = { playbackActions.shufflePlay(mediaItems) },
            floatingActionsContent = {
                PlaylistDownloadFloatingButton(mediaItems.toImmutableList())
                PrimaryButton(
                    icon = R.drawable.enqueue,
                    onClick = { playbackActions.enqueue(mediaItems) }
                )
            },
            headerContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Header(
                        title = playlist.name,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        SongListActionsRow(
                            filterQuery = filterQuery,
                            onFilterQueryChange = { filterQuery = it },
                            leadingContent = {
                                AnimatedVisibility(loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                }
                            },
                            trailingContent = {
                                HeaderSongSortBy(
                                    sortBy = sortBy,
                                    setSortBy = { sortBy = it },
                                    sortOrder = sortOrder,
                                    setSortOrder = { sortOrder = it }
                                )

                                HeaderIconButton(
                                    icon = R.drawable.ellipsis_horizontal,
                                    color = colorPalette.text,
                                    onClick = {
                                        menuState.display {
                                            Menu {
                                                playlist.browseId?.let { browseId ->
                                                    MenuEntry(
                                                        icon = R.drawable.sync,
                                                        text = stringResource(R.string.sync),
                                                        enabled = !loading,
                                                        onClick = {
                                                            menuState.hide()
                                                            coroutineScope.launch {
                                                                loading = true
                                                                viewModel.sync(playlist, browseId)
                                                                loading = false
                                                            }
                                                        }
                                                    )

                                                    songs.firstOrNull()?.id?.let { firstSongId ->
                                                        MenuEntry(
                                                            icon = R.drawable.play,
                                                            text = stringResource(R.string.watch_playlist_on_youtube),
                                                            onClick = {
                                                                menuState.hide()
                                                                binder?.player?.pause()
                                                                uriHandler.openUri(
                                                                    "https://youtube.com/watch?v=$firstSongId&list=${
                                                                        playlist.browseId.drop(2)
                                                                    }"
                                                                )
                                                            }
                                                        )

                                                        MenuEntry(
                                                            icon = R.drawable.musical_notes,
                                                            text = stringResource(R.string.open_in_youtube_music),
                                                            onClick = {
                                                                menuState.hide()
                                                                binder?.player?.pause()
                                                                if (
                                                                    !launchYouTubeMusic(
                                                                        context = context,
                                                                        endpoint = "watch?v=$firstSongId&list=${
                                                                            playlist.browseId.drop(2)
                                                                        }"
                                                                    )
                                                                ) context.toast(
                                                                    youtubeMusicNotInstalled
                                                                )
                                                            }
                                                        )
                                                    }
                                                }

                                                MenuEntry(
                                                    icon = R.drawable.share_social,
                                                    text = "Share Playlist Code",
                                                    onClick = {
                                                        menuState.hide()
                                                        coroutineScope.launch {
                                                            val res = SwaaraSharedPlaylists.publishPlaylist(context, playlist.id)
                                                            if (res.success && res.code != null) {
                                                                val sendIntent = android.content.Intent().apply {
                                                                    action = android.content.Intent.ACTION_SEND
                                                                    putExtra(
                                                                        android.content.Intent.EXTRA_TEXT,
                                                                        "Listen to '${playlist.name}' on A1 Swaara! Use share code: ${res.code}"
                                                                    )
                                                                    type = "text/plain"
                                                                }
                                                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Playlist Code"))
                                                            } else {
                                                                context.toast(res.message)
                                                            }
                                                        }
                                                    }
                                                )

                                                MenuEntry(
                                                    icon = R.drawable.pencil,
                                                    text = stringResource(R.string.rename),
                                                    onClick = {
                                                        menuState.hide()
                                                        isRenaming = true
                                                    }
                                                )

                                                MenuEntry(
                                                    icon = R.drawable.trash,
                                                    text = stringResource(R.string.delete),
                                                    onClick = {
                                                        menuState.hide()
                                                        isDeleting = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }

                    if (!isLandscape) thumbnailContent()
                }
            }
        ) {
            songCollectionItems(
                items = displayedSongs,
                isLoading = false,
                key = { _, song -> song.id },
                contentType = { _, song -> song },
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
                                    InPlaylistMediaItemMenu(
                                        playlistId = playlist.id,
                                        positionInPlaylist = songs.indexOf(song),
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                playbackActions.playAtIndex(mediaItems, index)
                            }
                        )
                        .then(
                            if (canReorder) Modifier
                                .animateItemPlacement(reorderingState)
                                .draggedItem(
                                    reorderingState = reorderingState,
                                    index = index
                                )
                            else Modifier
                        )
                        .songSwipeActions(
                            key = displayedSongs,
                            mediaItem = song.asMediaItem,
                            songToHide = song,
                            onSwipeLeftRequested = { hidingSong = it.id }
                        )
                        .background(colorPalette.background0),
                    song = song,
                    thumbnailSize = Dimensions.thumbnails.song,
                    onThumbnailContent = if (sortBy == SongSortBy.PlayTime) {
                        { SongTotalPlayTimeOverlay(song.totalPlayTimeMs) }
                    } else null,
                    trailingContent = {
                        if (canReorder) ReorderHandle(
                            reorderingState = reorderingState,
                            index = index
                        )
                    },
                    clip = !reorderingState.isDragging
                )
            }
        }
    }
}
