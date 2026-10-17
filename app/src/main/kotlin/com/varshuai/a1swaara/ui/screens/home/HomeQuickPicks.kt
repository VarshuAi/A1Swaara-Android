package com.varshuai.a1swaara.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerAwareWindowInsets
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.ShimmerHost
import com.varshuai.a1swaara.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.NonQueuedMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.RetryMessage
import com.varshuai.a1swaara.ui.components.themed.TextPlaceholder
import com.varshuai.a1swaara.ui.items.AlbumItem
import com.varshuai.a1swaara.ui.items.AlbumItemPlaceholder
import com.varshuai.a1swaara.ui.items.ArtistItem
import com.varshuai.a1swaara.ui.items.ArtistItemPlaceholder
import com.varshuai.a1swaara.ui.items.PlaylistItem
import com.varshuai.a1swaara.ui.items.PlaylistItemPlaceholder
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.items.SongItemPlaceholder
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.ui.state.LoadState
import com.varshuai.a1swaara.ui.state.contentOrNull
import com.varshuai.a1swaara.ui.viewmodels.HomeQuickPicksViewModel
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.center
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.rememberSnapLayoutInfo
import com.varshuai.a1swaara.utils.secondary
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.compose.persist.persist
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isLandscape
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun QuickPicks(
    onAlbumClick: (YoutubeMusicInnertube.AlbumItem) -> Unit,
    onArtistClick: (YoutubeMusicInnertube.ArtistItem) -> Unit,
    onPlaylistClick: (YoutubeMusicInnertube.PlaylistItem) -> Unit,
    onSearchClick: () -> Unit
) {
    val viewModel: HomeQuickPicksViewModel = viewModel(
        key = "home_quick_picks",
        factory = HomeQuickPicksViewModel.factory(LocalAppContainer.current.homeRepository)
    )
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    var trending by persist<Song?>("home/trending")

    val quickPicksState by viewModel.uiState.collectAsStateWithLifecycle()
    val relatedPage = quickPicksState.contentOrNull()

    LaunchedEffect(DataPreferences.shouldCacheQuickPicks) {
        if (!DataPreferences.shouldCacheQuickPicks) viewModel.clearCachedQuickPicks()
    }

    LaunchedEffect(DataPreferences.quickPicksSource) {
        suspend fun handleSong(song: Song?) {
            if (trending?.id != song?.id || quickPicksState is LoadState.Loading) {
                viewModel.load(videoId = song?.id ?: "J7p4bzqLvCw")
            }
            trending = song
        }

        when (DataPreferences.quickPicksSource) {
            DataPreferences.QuickPicksSource.Trending ->
                viewModel
                    .observeTrendingSong()
                    .distinctUntilChanged()
                    .collect(::handleSong)

            DataPreferences.QuickPicksSource.LastInteraction ->
                viewModel
                    .observeLastInteractionSong()
                    .distinctUntilChanged()
                    .collect(::handleSong)
        }
    }

    val scrollState = rememberScrollState()
    val quickPicksLazyGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    BoxWithConstraints {
        val quickPicksLazyGridItemWidthFactor =
            if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.75f

        val snapLayoutInfoProvider = rememberSnapLayoutInfo(
            lazyGridState = quickPicksLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
                (layoutSize * quickPicksLazyGridItemWidthFactor / 2f - itemSize / 2f)
            }
        )

        val itemInHorizontalGridWidth = maxWidth * quickPicksLazyGridItemWidthFactor

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )
        ) {
            val greeting = remember {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                when (hour) {
                    in 4..11 -> "Good morning"
                    in 12..16 -> "Good afternoon"
                    in 17..22 -> "Good evening"
                    else -> "Good night"
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(endPaddingValues)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "$greeting,",
                        style = typography.xxs.secondary.semiBold
                    )
                    BasicText(
                        text = "A1 Swara",
                        style = typography.l.semiBold
                    )
                }
            }

            Header(
                title = stringResource(R.string.quick_picks),
                modifier = Modifier.padding(endPaddingValues)
            )

            relatedPage?.let { related ->
                LazyHorizontalGrid(
                    state = quickPicksLazyGridState,
                    rows = GridCells.Fixed(4),
                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((Dimensions.thumbnails.song + Dimensions.items.verticalPadding * 2) * 4)
                ) {
                    trending?.let { song ->
                        item {
                            SongItem(
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem,
                                                    onRemoveFromQuickPicks = {
                                                        viewModel.removeFromQuickPicks(song.id)
                                                    }
                                                )
                                            }
                                        },
                                        onClick = {
                                            val mediaItem = song.asMediaItem
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(mediaItem)
                                            binder?.setupRadio(
                                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                            )
                                        }
                                    )
                                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                    .width(itemInHorizontalGridWidth),
                                song = song,
                                thumbnailSize = Dimensions.thumbnails.song,
                                trailingContent = {
                                    Image(
                                        painter = painterResource(R.drawable.star),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorPalette.accent),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                showDuration = false
                            )
                        }
                    }

                    items(
                        items = related.songs?.dropLast(if (trending == null) 0 else 1)
                            ?: emptyList(),
                        key = YoutubeMusicInnertube.SongItem::key
                    ) { song ->
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
                                        val mediaItem = song.asMediaItem
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(mediaItem)
                                        binder?.setupRadio(
                                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                        )
                                    }
                                )
                                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                .width(itemInHorizontalGridWidth),
                            showDuration = false
                        )
                    }
                }

                related.albums?.let { albums ->
                    BasicText(
                        text = stringResource(R.string.related_albums),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )

                    LazyRow(contentPadding = endPaddingValues) {
                        items(
                            items = albums,
                            key = YoutubeMusicInnertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable { onAlbumClick(album) }
                            )
                        }
                    }
                }

                related.artists?.let { artists ->
                    BasicText(
                        text = stringResource(R.string.similar_artists),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )

                    LazyRow(contentPadding = endPaddingValues) {
                        items(
                            items = artists,
                            key = YoutubeMusicInnertube.ArtistItem::key
                        ) { artist ->
                            ArtistItem(
                                artist = artist,
                                thumbnailSize = Dimensions.thumbnails.artist,
                                alternative = true,
                                modifier = Modifier.clickable { onArtistClick(artist) }
                            )
                        }
                    }
                }

                related.playlists?.let { playlists ->
                    BasicText(
                        text = stringResource(R.string.recommended_playlists),
                        style = typography.m.semiBold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 8.dp)
                    )

                    LazyRow(contentPadding = endPaddingValues) {
                        items(
                            items = playlists,
                            key = YoutubeMusicInnertube.PlaylistItem::key
                        ) { playlist ->
                            PlaylistItem(
                                playlist = playlist,
                                thumbnailSize = Dimensions.thumbnails.playlist,
                                alternative = true,
                                modifier = Modifier.clickable { onPlaylistClick(playlist) }
                            )
                        }
                    }
                }

                Unit
            } ?: (quickPicksState as? LoadState.Error)?.let {
                RetryMessage(
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            } ?: ShimmerHost {
                repeat(4) {
                    SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
                }

                TextPlaceholder(modifier = sectionTextModifier)

                Row {
                    repeat(2) {
                        AlbumItemPlaceholder(
                            thumbnailSize = Dimensions.thumbnails.album,
                            alternative = true
                        )
                    }
                }

                TextPlaceholder(modifier = sectionTextModifier)

                Row {
                    repeat(2) {
                        ArtistItemPlaceholder(
                            thumbnailSize = Dimensions.thumbnails.album,
                            alternative = true
                        )
                    }
                }

                TextPlaceholder(modifier = sectionTextModifier)

                Row {
                    repeat(2) {
                        PlaylistItemPlaceholder(
                            thumbnailSize = Dimensions.thumbnails.album,
                            alternative = true
                        )
                    }
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(
            scrollState = scrollState,
            icon = R.drawable.search,
            onClick = onSearchClick
        )
    }
}
