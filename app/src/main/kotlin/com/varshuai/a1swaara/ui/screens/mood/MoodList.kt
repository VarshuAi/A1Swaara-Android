package com.varshuai.a1swaara.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerAwareWindowInsets
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Mood
import com.varshuai.a1swaara.ui.components.ShimmerHost
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.HeaderPlaceholder
import com.varshuai.a1swaara.ui.components.themed.RetryMessage
import com.varshuai.a1swaara.ui.components.themed.TextPlaceholder
import com.varshuai.a1swaara.ui.items.AlbumItem
import com.varshuai.a1swaara.ui.items.AlbumItemPlaceholder
import com.varshuai.a1swaara.ui.items.ArtistItem
import com.varshuai.a1swaara.ui.items.PlaylistItem
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute
import com.varshuai.a1swaara.ui.screens.playlistRoute
import com.varshuai.a1swaara.ui.state.LoadState
import com.varshuai.a1swaara.ui.state.contentOrNull
import com.varshuai.a1swaara.ui.viewmodels.MoodListViewModel
import com.varshuai.a1swaara.utils.semiBold
import app.a1swaara.compose.persist.persist
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.requests.BrowseResult
import com.valentinilk.shimmer.shimmer

private const val DEFAULT_BROWSE_ID = "FEmusic_moods_and_genres_category"

@Composable
fun MoodList(
    mood: Mood,
    modifier: Modifier = Modifier
) = Column(modifier = modifier) {
    val viewModel: MoodListViewModel = viewModel(
        key = "mood_list:${mood.browseId ?: DEFAULT_BROWSE_ID}:${mood.params.orEmpty()}",
        factory = MoodListViewModel.factory(LocalAppContainer.current.moodRepository)
    )
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val browseId = mood.browseId ?: DEFAULT_BROWSE_ID
    var cachedMoodPage by persist<BrowseResult?>(
        tag = "playlist/mood/$browseId${mood.params?.let { "/$it" }.orEmpty()}"
    )
    val moodState by viewModel.uiState.collectAsStateWithLifecycle()
    val moodPage = moodState.contentOrNull() ?: cachedMoodPage

    LaunchedEffect(Unit) {
        viewModel.load(browseId, mood.params, cachedPage = cachedMoodPage)
    }
    LaunchedEffect(moodState) {
        (moodState as? LoadState.Content)?.value?.let { cachedMoodPage = it }
    }

    val lazyListState = rememberLazyListState()

    val endPaddingValues = windowInsets
        .only(WindowInsetsSides.End)
        .asPaddingValues()

    val contentPadding = windowInsets
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
        .asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    moodPage?.let { moodResult ->
        LazyColumn(
            state = lazyListState,
            contentPadding = contentPadding,
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Header(title = mood.name)
                }
            }

            moodResult.items.forEach { item ->
                item {
                    BasicText(
                        text = item.title.orEmpty(),
                        style = typography.m.semiBold,
                        modifier = sectionTextModifier
                    )
                }
                item {
                    LazyRow {
                        items(
                            items = item.items,
                            key = { it.key }
                        ) { childItem ->
                            if (childItem.key == DEFAULT_BROWSE_ID) return@items

                            when (childItem) {
                                is YoutubeMusicInnertube.AlbumItem -> AlbumItem(
                                    album = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.browseId?.let {
                                            albumRoute.global(it)
                                        }
                                    }
                                )

                                is YoutubeMusicInnertube.ArtistItem -> ArtistItem(
                                    artist = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.browseId?.let {
                                            artistRoute.global(it)
                                        }
                                    }
                                )

                                is YoutubeMusicInnertube.PlaylistItem -> PlaylistItem(
                                    playlist = childItem,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable {
                                        childItem.info?.endpoint?.let { endpoint ->
                                            playlistRoute.global(
                                                p0 = endpoint.browseId ?: return@clickable,
                                                p1 = endpoint.params,
                                                p2 = childItem.songCount?.let { it / 100 },
                                                p3 = true
                                            )
                                        }
                                    }
                                )

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    } ?: (moodState as? LoadState.Error)?.let {
        RetryMessage(
            onRetry = { viewModel.load(browseId, mood.params, force = true) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    } ?: ShimmerHost(modifier = Modifier.padding(contentPadding)) {
        HeaderPlaceholder(modifier = Modifier.shimmer())
        repeat(4) {
            TextPlaceholder(modifier = sectionTextModifier)
            Row {
                repeat(6) {
                    AlbumItemPlaceholder(
                        thumbnailSize = Dimensions.thumbnails.album,
                        alternative = true
                    )
                }
            }
        }
    }
}
