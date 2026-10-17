package com.varshuai.a1swaara.ui.screens.album

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.Album
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.ui.components.themed.Header
import com.varshuai.a1swaara.ui.components.themed.HeaderIconButton
import com.varshuai.a1swaara.ui.components.themed.HeaderPlaceholder
import com.varshuai.a1swaara.ui.components.themed.PlaylistInfo
import com.varshuai.a1swaara.ui.components.themed.Scaffold
import com.varshuai.a1swaara.ui.components.themed.adaptiveThumbnailContent
import com.varshuai.a1swaara.ui.items.AlbumItem
import com.varshuai.a1swaara.ui.items.AlbumItemPlaceholder
import com.varshuai.a1swaara.ui.screens.GlobalRoutes
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.searchresult.ItemsPage
import com.varshuai.a1swaara.ui.viewmodels.AlbumScreenViewModel
import com.varshuai.a1swaara.utils.asMediaItem
import app.a1swaara.compose.persist.PersistMapCleanup
import app.a1swaara.compose.persist.persist
import app.a1swaara.compose.persist.persistList
import app.a1swaara.compose.routing.RouteHandler
import app.a1swaara.core.ui.Dimensions
import app.a1swaara.core.ui.LocalAppearance
import app.a1swaara.core.ui.utils.stateFlowSaver
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Route
@Composable
fun AlbumScreen(browseId: String) {
    val viewModel: AlbumScreenViewModel = viewModel(
        key = "album:$browseId",
        factory = AlbumScreenViewModel.factory(LocalAppContainer.current.albumRepository)
    )
    val saveableStateHolder = rememberSaveableStateHolder()

    val tabIndexState = rememberSaveable(saver = stateFlowSaver()) { MutableStateFlow(0) }
    val tabIndex by tabIndexState.collectAsStateWithLifecycle()

    var album by persist<Album?>("album/$browseId/album")
    var albumPage by persist<YoutubeMusicInnertube.PlaylistOrAlbumPage?>("album/$browseId/albumPage")
    var songs by persistList<Song>("album/$browseId/songs")

    PersistMapCleanup(prefix = "album/$browseId/")

    LaunchedEffect(Unit) {
        viewModel
            .observeAlbumSongs(browseId)
            .distinctUntilChanged()
            .combine(
                viewModel
                    .observeAlbum(browseId)
                    .distinctUntilChanged()
                    .cancellable()
            ) { currentSongs, currentAlbum ->
                album = currentAlbum
                songs = currentSongs.toImmutableList()

                if (currentAlbum?.timestamp != null && currentSongs.isNotEmpty()) return@combine

                withContext(Dispatchers.IO) {
                    viewModel.fetchAlbumPage(browseId)
                        ?.onSuccess { newAlbumPage ->
                            newAlbumPage?.let { page ->
                                albumPage = page
                                viewModel.replaceAlbumFromPage(
                                    browseId = browseId,
                                    bookmarkedAt = album?.bookmarkedAt,
                                    page = page
                                )
                            }
                        }?.exceptionOrNull()?.printStackTrace()
                }
            }.collect()
    }

    RouteHandler {
        GlobalRoutes()

        Content {
            val headerContent: @Composable (
                beforeContent: (@Composable () -> Unit)?,
                afterContent: (@Composable () -> Unit)?
            ) -> Unit = { beforeContent, afterContent ->
                if (album?.timestamp == null) HeaderPlaceholder(modifier = Modifier.shimmer())
                else {
                    val (colorPalette) = LocalAppearance.current
                    val context = LocalContext.current

                    Header(title = album?.title ?: stringResource(R.string.unknown)) {
                        beforeContent?.invoke()

                        Spacer(modifier = Modifier.weight(1f))

                        afterContent?.invoke()

                        HeaderIconButton(
                            icon = if (album?.bookmarkedAt == null) R.drawable.bookmark_outline
                            else R.drawable.bookmark,
                            color = colorPalette.accent,
                            onClick = { viewModel.toggleBookmark(album) }
                        )

                        HeaderIconButton(
                            icon = R.drawable.share_social,
                            color = colorPalette.text,
                            onClick = {
                                album?.shareUrl?.let { url ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }

                                    context.startActivity(
                                        Intent.createChooser(sendIntent, null)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            val thumbnailContent = adaptiveThumbnailContent(
                isLoading = album?.timestamp == null,
                url = album?.thumbnailUrl
            )

            Scaffold(
                key = "album",
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChange = { newTab -> tabIndexState.update { newTab } },
                tabColumnContent = {
                    tab(0, R.string.songs, R.drawable.musical_notes, canHide = false)
                    tab(1, R.string.other_versions, R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> AlbumSongs(
                            songs = songs,
                            headerContent = headerContent,
                            thumbnailContent = thumbnailContent,
                            afterHeaderContent = {
                                if (songs.isNotEmpty()) {
                                    if (album == null) PlaylistInfo(playlist = albumPage)
                                    else PlaylistInfo(playlist = album)
                                }
                            }
                        )

                        1 -> {
                            ItemsPage(
                                tag = "album/$browseId/alternatives",
                                header = headerContent,
                                initialPlaceholderCount = 1,
                                continuationPlaceholderCount = 1,
                                emptyItemsText = stringResource(R.string.no_alternative_version),
                                provider = albumPage?.let {
                                    {
                                        Result.success(
                                            YoutubeMusicInnertube.ItemsPage(
                                                items = albumPage?.otherVersions,
                                                continuation = null
                                            )
                                        )
                                    }
                                },
                                itemContent = { album ->
                                    AlbumItem(
                                        album = album,
                                        thumbnailSize = Dimensions.thumbnails.album,
                                        modifier = Modifier.clickable { albumRoute(album.key) }
                                    )
                                },
                                itemPlaceholderContent = {
                                    AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
