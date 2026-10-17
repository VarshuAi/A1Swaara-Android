package com.varshuai.a1swaara.ui.screens.artist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.varshuai.a1swaara.LocalPlayerAwareWindowInsets
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.ui.components.LocalMenuState
import com.varshuai.a1swaara.ui.components.ShimmerHost
import com.varshuai.a1swaara.ui.components.themed.Attribution
import com.varshuai.a1swaara.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.varshuai.a1swaara.ui.components.themed.LayoutWithAdaptiveThumbnail
import com.varshuai.a1swaara.ui.components.themed.NonQueuedMediaItemMenu
import com.varshuai.a1swaara.ui.components.themed.SecondaryTextButton
import com.varshuai.a1swaara.ui.components.themed.TextPlaceholder
import com.varshuai.a1swaara.ui.items.AlbumItem
import com.varshuai.a1swaara.ui.items.AlbumItemPlaceholder
import com.varshuai.a1swaara.ui.items.SongItem
import com.varshuai.a1swaara.ui.items.SongItemPlaceholder
import com.varshuai.a1swaara.ui.modifiers.songSwipeActions
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.medium
import com.varshuai.a1swaara.utils.secondary
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.isLandscape
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint

private val sectionTextModifier = Modifier
    .padding(horizontal = 16.dp)
    .padding(top = 24.dp, bottom = 8.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistOverview(
    youtubeArtistPage: YoutubeMusicInnertube.ArtistPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) = LayoutWithAdaptiveThumbnail(
    thumbnailContent = thumbnailContent,
    modifier = modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val scrollState = rememberScrollState()

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
            Box(modifier = Modifier.padding(endPaddingValues)) {
                headerContent {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        youtubeArtistPage?.shuffleEndpoint?.let { endpoint ->
                            SecondaryTextButton(
                                text = stringResource(R.string.shuffle),
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.playRadio(endpoint)
                                }
                            )
                        }
                        youtubeArtistPage?.subscribersCountText?.let { subscribers ->
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                BasicText(
                                    text = stringResource(R.string.format_subscribers, subscribers),
                                    style = typography.xxs.medium.copy(color = Color(0xFF20CFFF))
                                )
                            }
                        }
                    }
                }
            }

            if (!isLandscape) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient Glow behind artist portrait
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    listOf(
                                        Color(0xFF8B35FF).copy(alpha = 0.45f),
                                        Color(0xFFFF2DAA).copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                androidx.compose.ui.graphics.Brush.sweepGradient(
                                    listOf(
                                        Color(0xFFFF2DAA),
                                        Color(0xFF8B35FF),
                                        Color(0xFF20CFFF),
                                        Color(0xFFFF2DAA)
                                    )
                                ),
                                CircleShape
                            )
                    ) {
                        thumbnailContent()
                    }
                }
            }

            youtubeArtistPage?.let { artist ->
                artist.songs?.let { songs ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.songs),
                            style = typography.m.semiBold.copy(color = Color.White),
                            modifier = sectionTextModifier
                        )

                        artist.songsEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary.copy(color = colorPalette.accent),
                                modifier = sectionTextModifier.clickable(onClick = onViewAllSongsClick)
                            )
                        }
                    }

                    songs.forEach { song ->
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
                                .songSwipeActions(
                                    key = songs,
                                    mediaItem = song.asMediaItem
                                )
                                .padding(endPaddingValues)
                        )
                    }
                }

                artist.albums?.let { albums ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.albums),
                            style = typography.m.semiBold.copy(color = Color.White),
                            modifier = sectionTextModifier
                        )

                        artist.albumsEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary.copy(color = colorPalette.accent),
                                modifier = sectionTextModifier.clickable(onClick = onViewAllAlbumsClick)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = endPaddingValues,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = albums,
                            key = YoutubeMusicInnertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable {
                                    onAlbumClick(album.key)
                                }
                            )
                        }
                    }
                }

                artist.singles?.let { singles ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.singles),
                            style = typography.m.semiBold.copy(color = Color.White),
                            modifier = sectionTextModifier
                        )

                        artist.singlesEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary.copy(color = colorPalette.accent),
                                modifier = sectionTextModifier.clickable(onClick = onViewAllSinglesClick)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = endPaddingValues,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = singles,
                            key = YoutubeMusicInnertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable(onClick = { onAlbumClick(album.key) })
                            )
                        }
                    }
                }

                artist.description?.let { description ->
                    Attribution(
                        text = description,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }

                Unit
            } ?: ArtistOverviewBodyPlaceholder()
        }

        youtubeArtistPage?.radioEndpoint?.let { endpoint ->
            FloatingActionsContainerWithScrollToTop(
                scrollState = scrollState,
                icon = R.drawable.radio,
                onClick = {
                    binder?.stopRadio()
                    binder?.playRadio(endpoint)
                }
            )
        }
    }
}

@Composable
fun ArtistOverviewBodyPlaceholder(modifier: Modifier = Modifier) = ShimmerHost(
    modifier = modifier
) {
    TextPlaceholder(modifier = sectionTextModifier)

    repeat(5) {
        SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
    }

    repeat(2) {
        TextPlaceholder(modifier = sectionTextModifier)

        Row {
            repeat(2) {
                AlbumItemPlaceholder(
                    thumbnailSize = Dimensions.thumbnails.album,
                    alternative = true
                )
            }
        }
    }
}
