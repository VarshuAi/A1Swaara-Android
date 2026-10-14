package com.varshuai.a1swaara.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.SearchQuery
import com.varshuai.a1swaara.ui.components.RaagaCyan
import com.varshuai.a1swaara.ui.components.RaagaPink
import com.varshuai.a1swaara.ui.components.RaagaPurple
import com.varshuai.a1swaara.ui.screens.LocalOpenNowPlaying
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.playTrack
import com.varshuai.a1swaara.utils.thumbnail
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute
import com.varshuai.a1swaara.ui.screens.playlistRoute
import androidx.compose.ui.platform.LocalContext
import com.varshuai.a1swaara.providers.instagram.InstagramTrack
import com.varshuai.a1swaara.providers.instagram.InstagramMusicClient
import com.varshuai.a1swaara.providers.instagram.InstagramTrackResolver

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RaagaSearchScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val binder = LocalPlayerServiceBinder.current
    val openNowPlaying = LocalOpenNowPlaying.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = remember { listOf("All", "Songs", "Reels", "Albums", "Artists", "Playlists") }

    val recentQueries by container.onlineSearchRepository
        .observeHistory("")
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var searchResults by remember { mutableStateOf<List<YoutubeMusicInnertube.SongItem>>(emptyList()) }
    var reelsResults by remember { mutableStateOf<List<InstagramTrack>>(emptyList()) }
    var artistResults by remember { mutableStateOf<List<YoutubeMusicInnertube.ArtistItem>>(emptyList()) }
    var albumResults by remember { mutableStateOf<List<YoutubeMusicInnertube.AlbumItem>>(emptyList()) }
    var playlistResults by remember { mutableStateOf<List<YoutubeMusicInnertube.PlaylistItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Instant Real-Time Auto-Search as the user types
    LaunchedEffect(searchQuery, selectedFilterIndex) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            reelsResults = emptyList()
            artistResults = emptyList()
            albumResults = emptyList()
            playlistResults = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        delay(220) // Fast debounce
        isLoading = true
        withContext(Dispatchers.IO) {
            val q = searchQuery.trim()
            when (selectedFilterIndex) {
                2 -> {
                    // Reels Sounds Filter
                    val reels = runCatching {
                        InstagramMusicClient.searchMusic(q)
                    }.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        reelsResults = reels
                        searchResults = emptyList()
                        artistResults = emptyList()
                        albumResults = emptyList()
                        playlistResults = emptyList()
                        isLoading = false
                    }
                }
                3 -> {
                    // Albums Filter
                    val albums = runCatching {
                        container.searchResultRepository.searchAlbums(q, null)?.getOrNull()?.items
                    }.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        albumResults = albums
                        reelsResults = emptyList()
                        searchResults = emptyList()
                        artistResults = emptyList()
                        playlistResults = emptyList()
                        isLoading = false
                    }
                }
                4 -> {
                    // Artists Filter
                    val artists = runCatching {
                        container.searchResultRepository.searchArtists(q, null)?.getOrNull()?.items
                    }.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        artistResults = artists
                        reelsResults = emptyList()
                        searchResults = emptyList()
                        albumResults = emptyList()
                        playlistResults = emptyList()
                        isLoading = false
                    }
                }
                5 -> {
                    // Playlists Filter
                    val playlists = runCatching {
                        container.searchResultRepository.searchPlaylists(q, null)?.getOrNull()?.items
                    }.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        playlistResults = playlists
                        reelsResults = emptyList()
                        searchResults = emptyList()
                        artistResults = emptyList()
                        albumResults = emptyList()
                        isLoading = false
                    }
                }
                else -> {
                    // All (0) or Songs (1)
                    val songs = runCatching {
                        container.searchResultRepository.searchSongs(q, null)?.getOrNull()?.items
                    }.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        reelsResults = emptyList()
                    }
                    val artists = if (selectedFilterIndex == 0) {
                        runCatching {
                            container.searchResultRepository.searchArtists(q, null)?.getOrNull()?.items?.take(1)
                        }.getOrNull() ?: emptyList()
                    } else emptyList()
                    val albums = if (selectedFilterIndex == 0) {
                        runCatching {
                            container.searchResultRepository.searchAlbums(q, null)?.getOrNull()?.items?.take(2)
                        }.getOrNull() ?: emptyList()
                    } else emptyList()

                    withContext(Dispatchers.Main) {
                        searchResults = songs
                        artistResults = artists
                        albumResults = albums
                        playlistResults = emptyList()
                        isLoading = false
                    }
                }
            }
        }
    }

    val performExplicitSearch: (String) -> Unit = { query ->
        if (query.isNotBlank()) {
            focusManager.clearFocus()
            isLoading = true
            coroutineScope.launch {
                val q = query.trim()
                container.onlineSearchRepository.saveHistory(q)
                withContext(Dispatchers.IO) {
                    when (selectedFilterIndex) {
                        2 -> {
                            val albums = runCatching {
                                container.searchResultRepository.searchAlbums(q, null)?.getOrNull()?.items
                            }.getOrNull() ?: emptyList()
                            withContext(Dispatchers.Main) {
                                albumResults = albums
                                searchResults = emptyList()
                                artistResults = emptyList()
                                playlistResults = emptyList()
                                isLoading = false
                            }
                        }
                        3 -> {
                            val artists = runCatching {
                                container.searchResultRepository.searchArtists(q, null)?.getOrNull()?.items
                            }.getOrNull() ?: emptyList()
                            withContext(Dispatchers.Main) {
                                artistResults = artists
                                searchResults = emptyList()
                                albumResults = emptyList()
                                playlistResults = emptyList()
                                isLoading = false
                            }
                        }
                        4 -> {
                            val playlists = runCatching {
                                container.searchResultRepository.searchPlaylists(q, null)?.getOrNull()?.items
                            }.getOrNull() ?: emptyList()
                            withContext(Dispatchers.Main) {
                                playlistResults = playlists
                                searchResults = emptyList()
                                artistResults = emptyList()
                                albumResults = emptyList()
                                isLoading = false
                            }
                        }
                        else -> {
                            val songs = runCatching {
                                container.searchResultRepository.searchSongs(q, null)?.getOrNull()?.items
                            }.getOrNull() ?: emptyList()
                            val artists = if (selectedFilterIndex == 0) {
                                runCatching {
                                    container.searchResultRepository.searchArtists(q, null)?.getOrNull()?.items?.take(1)
                                }.getOrNull() ?: emptyList()
                            } else emptyList()
                            val albums = if (selectedFilterIndex == 0) {
                                runCatching {
                                    container.searchResultRepository.searchAlbums(q, null)?.getOrNull()?.items?.take(2)
                                }.getOrNull() ?: emptyList()
                            } else emptyList()

                            withContext(Dispatchers.Main) {
                                searchResults = songs
                                artistResults = artists
                                albumResults = albums
                                playlistResults = emptyList()
                                isLoading = false
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07070B))
            .padding(bottom = 120.dp)
    ) {
        // Search Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search songs, artists, albums...", color = Color.White.copy(alpha = 0.40f), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Search",
                        tint = RaagaPurple,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performExplicitSearch(searchQuery) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RaagaPurple,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color(0xFF141424),
                    unfocusedContainerColor = Color(0xFF12121E)
                ),
                shape = RoundedCornerShape(22.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { index, filter ->
                    val isSelected = selectedFilterIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(listOf(RaagaPink, RaagaPurple))
                                else Brush.horizontalGradient(listOf(Color(0xFF161626), Color(0xFF161626)))
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedFilterIndex = index
                                if (searchQuery.isNotBlank()) performExplicitSearch(searchQuery)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Search Content / Results / History
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RaagaPurple)
            }
        } else if (reelsResults.isNotEmpty() || artistResults.isNotEmpty() || albumResults.isNotEmpty() || playlistResults.isNotEmpty() || searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (reelsResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "Reels Sounds (${reelsResults.size})",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(reelsResults, key = { it.id }) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131322))
                                .clickable {
                                    focusManager.clearFocus()
                                    container.onlineSearchRepository.saveHistory(track.title)
                                    coroutineScope.launch {
                                        openNowPlaying()
                                        InstagramTrackResolver.resolveAndPlay(
                                            context = context,
                                            binder = binder,
                                            container = container,
                                            track = track
                                        )
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = track.coverArtworkUri,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${track.displayArtist} • ${track.reelsCountFormatted ?: "Reels Viral"}",
                                    color = RaagaPink,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                painter = painterResource(R.drawable.play),
                                contentDescription = "Play Track",
                                tint = RaagaCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (artistResults.isNotEmpty()) {
                    item {
                        Text(
                            text = if (selectedFilterIndex == 4) "Artists (${artistResults.size})" else "Top Artist",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(artistResults) { artist ->
                        val artistName = artist.info?.name ?: "Artist"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131322))
                                .clickable {
                                    focusManager.clearFocus()
                                    container.onlineSearchRepository.saveHistory(artistName)
                                    artistRoute.global(artist.key)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(RaagaPink, RaagaPurple))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = artistName.take(1),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                AsyncImage(
                                    model = artist.thumbnail?.url?.thumbnail(800),
                                    contentDescription = artistName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = artistName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Artist Profile • Tap to view discography",
                                    color = RaagaCyan,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                painter = painterResource(R.drawable.chevron_forward),
                                contentDescription = "View Artist",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (albumResults.isNotEmpty()) {
                    item {
                        Text(
                            text = if (selectedFilterIndex == 3) "Albums (${albumResults.size})" else "Top Albums",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp, top = if (artistResults.isNotEmpty()) 8.dp else 0.dp)
                        )
                    }

                    items(albumResults) { album ->
                        val albumTitle = album.info?.name ?: "Album"
                        val author = album.authors?.joinToString { it.name.orEmpty() }?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Artist"
                        val year = album.year?.let { " • $it" } ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131322))
                                .clickable {
                                    focusManager.clearFocus()
                                    container.onlineSearchRepository.saveHistory(albumTitle)
                                    albumRoute.global(album.key)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = album.thumbnail?.url?.thumbnail(800),
                                    contentDescription = albumTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = albumTitle,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$author$year • Album",
                                    color = RaagaCyan,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.chevron_forward),
                                contentDescription = "View Album",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (playlistResults.isNotEmpty()) {
                    item {
                        Text(
                            text = if (selectedFilterIndex == 5) "Playlists (${playlistResults.size})" else "Top Playlists",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp, top = if (artistResults.isNotEmpty() || albumResults.isNotEmpty()) 8.dp else 0.dp)
                        )
                    }

                    items(playlistResults) { playlist ->
                        val playlistTitle = playlist.info?.name ?: "Playlist"
                        val author = playlist.channel?.name ?: "Curated Playlist"
                        val songsCount = playlist.songCount?.let { " • $it tracks" } ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF131322))
                                .clickable {
                                    focusManager.clearFocus()
                                    container.onlineSearchRepository.saveHistory(playlistTitle)
                                    playlistRoute.global(playlist.key, null, null, false)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.DarkGray)
                            ) {
                                AsyncImage(
                                    model = playlist.thumbnail?.url?.thumbnail(800),
                                    contentDescription = playlistTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlistTitle,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$author$songsCount • Playlist",
                                    color = RaagaPink,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                painter = painterResource(R.drawable.chevron_forward),
                                contentDescription = "View Playlist",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (searchResults.isNotEmpty() && (selectedFilterIndex == 0 || selectedFilterIndex == 1)) {
                    if (selectedFilterIndex == 0 && (artistResults.isNotEmpty() || albumResults.isNotEmpty() || playlistResults.isNotEmpty())) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Songs",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                items(searchResults) { song ->
                    val videoId = song.key
                    val songTitle = song.info?.name ?: "Unknown Title"
                    val artistNames = song.authors?.joinToString { it.name.orEmpty() }?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Artist"
                    val highResThumbnail = song.thumbnail?.url?.thumbnail(800) ?: "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF131322))
                            .clickable {
                                focusManager.clearFocus()
                                openNowPlaying()
                                coroutineScope.launch {
                                    container.onlineSearchRepository.saveHistory(songTitle)
                                    val activeBinder = binder ?: snapshotFlow { binder }.filterNotNull().first()
                                    activeBinder.playTrack(
                                        videoId = videoId,
                                        title = songTitle,
                                        artist = artistNames,
                                        thumbnailUrl = highResThumbnail
                                    )
                                }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray)
                        ) {
                            AsyncImage(
                                model = highResThumbnail,
                                contentDescription = songTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = songTitle,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = artistNames,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(RaagaPink, RaagaPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else if (searchQuery.isEmpty()) {
            // Recent Searches & Trending Suggestions
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (recentQueries.isNotEmpty()) {
                    Text(
                        text = "Recent Searches",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentQueries.take(8).forEach { item: SearchQuery ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF161626))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        searchQuery = item.query
                                        performExplicitSearch(item.query)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.query,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Trending Now",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val trendingPicks = listOf("Arijit Singh Hits", "Kannada Golden Hits", "Diljit Dosanjh", "Anirudh BGM", "Late Night Lofi", "Sid Sriram Hits")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    trendingPicks.forEach { pick ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF161626))
                                .border(1.dp, RaagaCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .clickable {
                                searchQuery = pick
                                performExplicitSearch(pick)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pick,
                                color = RaagaCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs found for '$searchQuery'",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
