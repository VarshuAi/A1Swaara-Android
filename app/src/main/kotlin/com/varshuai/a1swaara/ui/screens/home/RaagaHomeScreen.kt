package com.varshuai.a1swaara.ui.screens.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.ui.screens.LocalOpenNowPlaying
import com.varshuai.a1swaara.updater.SwaaraUpdateManager
import com.varshuai.a1swaara.utils.intent
import com.varshuai.a1swaara.utils.playTrack
import com.varshuai.a1swaara.utils.toast
import com.varshuai.a1swaara.ui.components.RaagaBrandLoader
import com.varshuai.a1swaara.ui.components.RaagaHomeFeedSkeleton
import com.varshuai.a1swaara.ui.components.RaagaDisplayItem
import com.varshuai.a1swaara.ui.components.RaagaGlassContainer
import com.varshuai.a1swaara.ui.components.RaagaLanguageChips
import com.varshuai.a1swaara.ui.components.RaagaQuickGridItem
import com.varshuai.a1swaara.ui.components.RaagaTrendingShelf
import com.varshuai.a1swaara.ui.components.SwaaraCuratedMix
import com.varshuai.a1swaara.ui.components.SwaaraCuratedShelf
import com.varshuai.a1swaara.ui.components.SwaaraObsidian
import com.varshuai.a1swaara.ui.components.SwaaraDeepSurface
import com.varshuai.a1swaara.ui.components.SwaaraElevatedSurface
import com.varshuai.a1swaara.ui.components.SwaaraPrimaryText
import com.varshuai.a1swaara.ui.components.SwaaraSecondaryText
import com.varshuai.a1swaara.ui.components.SwaaraMagenta
import com.varshuai.a1swaara.ui.components.SwaaraViolet
import com.varshuai.a1swaara.ui.components.SwaaraCyan
import com.varshuai.a1swaara.utils.forcePlay
import com.varshuai.a1swaara.utils.thumbnail
import app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.varshuai.a1swaara.ui.screens.artistRoute
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar
import com.varshuai.a1swaara.providers.instagram.InstagramTrack
import com.varshuai.a1swaara.providers.instagram.InstagramMusicClient
import com.varshuai.a1swaara.providers.instagram.InstagramTrackResolver
import com.varshuai.a1swaara.ui.components.RaagaReelsShelf
import com.varshuai.a1swaara.cloud.SwaaraCommunityCharts
import com.varshuai.a1swaara.cloud.TrendingSong
import com.varshuai.a1swaara.cloud.SwaaraListenTogether
import com.varshuai.a1swaara.cloud.RoomState
import com.varshuai.a1swaara.cloud.SwaaraRemoteConfig
import com.varshuai.a1swaara.cloud.SwaaraAnnouncement
import com.varshuai.a1swaara.ui.components.RaagaCommunityShelf
import com.varshuai.a1swaara.ui.components.SwaaraJamRoomDialog


data class CuratedMix(
    val title: String,
    val description: String,
    val query: String,
    val gradient: List<Color>,
    val iconId: Int
)

data class RaagaArtistDisplayItem(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null
)

@Composable
fun RaagaHomeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val binder = LocalPlayerServiceBinder.current
    val openNowPlaying = LocalOpenNowPlaying.current
    val coroutineScope = rememberCoroutineScope()

    // Synchronously read persistent user preferences
    val initialPrefs = remember {
        val p = context.getSharedPreferences("swaara_prefs", Context.MODE_PRIVATE)
        val name = p.getString("user_name", "Music Lover") ?: "Music Lover"
        val activeLang = p.getString("active_language", null)
            ?: p.getStringSet("selected_languages", null)?.firstOrNull()
            ?: "for_you"
        Pair(name, activeLang)
    }

    var userName by remember { mutableStateOf(initialPrefs.first) }
    var selectedLanguage by remember { mutableStateOf(initialPrefs.second) }
    var isLoadingFeed by remember { mutableStateOf(true) }

    var liveTrendingSongs by remember { mutableStateOf<List<RaagaDisplayItem>>(emptyList()) }
    var liveArtists by remember { mutableStateOf<List<RaagaArtistDisplayItem>>(emptyList()) }
    var quickGridItems by remember { mutableStateOf<List<RaagaDisplayItem>>(emptyList()) }
    var viralSongs by remember { mutableStateOf<List<RaagaDisplayItem>>(emptyList()) }
    var topTwentyList by remember { mutableStateOf<List<RaagaDisplayItem>>(emptyList()) }

    var reelsTracks by remember { mutableStateOf<List<InstagramTrack>>(emptyList()) }
    var isLoadingReels by remember { mutableStateOf(true) }
    var isResolvingReel by remember { mutableStateOf(false) }

    var communitySongs by remember { mutableStateOf<List<TrendingSong>>(emptyList()) }
    var showJamDialog by remember { mutableStateOf(false) }

    val currentJamRoom by SwaaraListenTogether.currentRoom.collectAsState()
    val remoteConfigState by SwaaraRemoteConfig.configState.collectAsState()

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val tracks = InstagramMusicClient.getTrendingReelsPlaylist()
            withContext(Dispatchers.Main) {
                reelsTracks = tracks
                isLoadingReels = false
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            val trending = SwaaraCommunityCharts.getTrendingSongs(limit = 20)
            withContext(Dispatchers.Main) {
                communitySongs = trending
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            SwaaraRemoteConfig.fetchAndApply(context)
        }
    }

    if (showJamDialog) {
        SwaaraJamRoomDialog(onDismiss = { showJamDialog = false })
    }

    val curatedMixes = remember(selectedLanguage) {
        val isForYou = selectedLanguage.equals("for_you", ignoreCase = true)
        val langTitle = if (isForYou) "Daily" else selectedLanguage.replaceFirstChar { it.uppercase() }
        val prefix = if (isForYou) "" else "$selectedLanguage "
        listOf(
            SwaaraCuratedMix(
                id = "pulse",
                title = if (isForYou) "Daily Flow" else "$langTitle Pulse",
                description = if (isForYou) "Curated hits tailored for your vibe" else "Today's biggest $langTitle tracks",
                gradientColors = listOf(Color(0xFF2A0845), Color(0xFF6441A5)),
                query = if (isForYou) "latest top trending music hits official" else "$prefix top trending hits",
                iconId = R.drawable.sparkles
            ),
            SwaaraCuratedMix(
                id = "midnight",
                title = "Midnight Swaara",
                description = "Late-night atmospheric sounds",
                gradientColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
                query = if (isForYou) "lofi beats chill late night relaxing" else "$prefix late night lofi chill",
                iconId = R.drawable.musical_notes
            ),
            SwaaraCuratedMix(
                id = "chill",
                title = "Chill Flow",
                description = "Relaxing beats & smooth vocals",
                gradientColors = listOf(Color(0xFF134E5E), Color(0xFF71B280)),
                query = if (isForYou) "acoustic chill relaxing songs" else "$prefix acoustic chill relaxing",
                iconId = R.drawable.heart
            ),
            SwaaraCuratedMix(
                id = "favourites",
                title = "All-Time Favourites",
                description = "Timeless songs you love",
                gradientColors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)),
                query = if (isForYou) "all time greatest hits evergreen songs" else "$prefix all time evergreen classics",
                iconId = R.drawable.disc
            )
        )
    }

    // Play Function with Infinite YouTube Radio Auto-Queue
    val playSongWithInfiniteRadio: (String, String, String?, String?) -> Unit = { videoId, songTitle, artist, thumb ->
        if (SwaaraUpdateManager.isUpdateAvailable.value && SwaaraUpdateManager.isMandatoryLockActive.value) {
            context.toast("Online music is locked. Please update A1 Swaara to stream online tracks.")
        } else {
            openNowPlaying()
            if (binder != null) {
                binder.playTrack(
                    videoId = videoId,
                    title = songTitle,
                    artist = artist,
                    thumbnailUrl = thumb
                )
            } else {
                try {
                    context.startService(context.intent<PlayerService>())
                } catch (_: Exception) {}
                coroutineScope.launch {
                    val activeBinder = snapshotFlow { binder }.filterNotNull().first()
                    activeBinder.playTrack(
                        videoId = videoId,
                        title = songTitle,
                        artist = artist,
                        thumbnailUrl = thumb
                    )
                }
            }
        }
    }

    // Load Live YouTube Music Feed for Selected Language & Personal History
    val loadLiveFeed: (String) -> Unit = { lang ->
        isLoadingFeed = true
        coroutineScope.launch(Dispatchers.IO) {
            // 1. Fetch real user history from Database for Jump Back In
            val userHistoryEvents = runCatching {
                Database.events().first()
            }.getOrNull().orEmpty()

            val historyDisplayItems = userHistoryEvents
                .map { it.song }
                .distinctBy { it.id }
                .take(6)
                .map { song ->
                    RaagaDisplayItem(
                        id = song.id,
                        title = song.title,
                        subtitle = song.artistsText ?: "Artist",
                        thumbnailUrl = song.thumbnailUrl ?: "https://i.ytimg.com/vi/${song.id}/maxresdefault.jpg"
                    )
                }

            // 2. Build smart queries based on selected category / language
            val isForYou = lang.equals("for_you", ignoreCase = true)

            // If "For You", determine if we can extract top artists/keywords from history
            val topHistoryArtist = if (isForYou && historyDisplayItems.isNotEmpty()) {
                historyDisplayItems.firstOrNull()?.subtitle?.split(",")?.firstOrNull()?.trim()
            } else null

            val trendingQuery = when {
                isForYou && !topHistoryArtist.isNullOrBlank() -> "$topHistoryArtist latest songs hits"
                isForYou -> "top trending music hits official"
                lang.equals("english", ignoreCase = true) -> "trending top hits official"
                else -> "$lang latest trending songs official"
            }

            val viralQuery = when {
                isForYou -> "viral music hits today"
                lang.equals("english", ignoreCase = true) -> "top viral songs"
                else -> "$lang viral hits chart"
            }

            val topChartQuery = when {
                isForYou -> "billboard top 50 global hits"
                lang.equals("english", ignoreCase = true) -> "billboard top 50"
                else -> "$lang top 50 songs chart"
            }

            val artistQuery = when {
                isForYou -> "popular global music artists"
                lang.equals("english", ignoreCase = true) -> "popular music artists"
                else -> "$lang popular playback singers"
            }

            val liveSongsResult = container.searchResultRepository.searchSongs(trendingQuery, null)?.getOrNull()?.items
            val viralResult = container.searchResultRepository.searchSongs(viralQuery, null)?.getOrNull()?.items
            val topChartResult = container.searchResultRepository.searchSongs(topChartQuery, null)?.getOrNull()?.items
            val liveArtistsResult = runCatching {
                container.searchResultRepository.searchArtists(artistQuery, null)?.getOrNull()?.items
            }.getOrNull()

            withContext(Dispatchers.Main) {
                val mappedSongs = (!liveSongsResult.isNullOrEmpty()).let { hasSongs ->
                    if (hasSongs) {
                        liveSongsResult!!.map { item ->
                            RaagaDisplayItem(
                                id = item.key,
                                title = item.info?.name ?: "Unknown Title",
                                subtitle = item.authors?.joinToString { it.name.orEmpty() }?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Various Artists",
                                thumbnailUrl = item.thumbnail?.url?.thumbnail(800) ?: "https://i.ytimg.com/vi/${item.key}/maxresdefault.jpg"
                            )
                        }
                    } else emptyList()
                }

                liveTrendingSongs = mappedSongs

                // Set Jump Back In: Prioritize real user listening history, fallback to trending
                if (historyDisplayItems.isNotEmpty()) {
                    if (historyDisplayItems.size < 6 && mappedSongs.isNotEmpty()) {
                        val historyIds = historyDisplayItems.map { it.id }.toSet()
                        val filler = mappedSongs.filter { it.id !in historyIds }.take(6 - historyDisplayItems.size)
                        quickGridItems = historyDisplayItems + filler
                    } else {
                        quickGridItems = historyDisplayItems
                    }
                } else {
                    quickGridItems = mappedSongs.take(6)
                }

                if (!liveArtistsResult.isNullOrEmpty()) {
                    liveArtists = liveArtistsResult.take(12).map { item ->
                        RaagaArtistDisplayItem(
                            id = item.key,
                            name = item.info?.name ?: "Artist",
                            thumbnailUrl = item.thumbnail?.url?.thumbnail(800)
                        )
                    }
                }

                if (!viralResult.isNullOrEmpty()) {
                    viralSongs = viralResult.map { item ->
                        RaagaDisplayItem(
                            id = item.key,
                            title = item.info?.name ?: "Viral Hit",
                            subtitle = item.authors?.joinToString { it.name.orEmpty() }?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Artist",
                            thumbnailUrl = item.thumbnail?.url?.thumbnail(800) ?: "https://i.ytimg.com/vi/${item.key}/maxresdefault.jpg"
                        )
                    }
                }

                val allSongs = (topChartResult ?: liveSongsResult).orEmpty()
                if (allSongs.isNotEmpty()) {
                    topTwentyList = allSongs.take(20).map { item ->
                        RaagaDisplayItem(
                            id = item.key,
                            title = item.info?.name ?: "Top Song",
                            subtitle = item.authors?.joinToString { it.name.orEmpty() }?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Artist",
                            thumbnailUrl = item.thumbnail?.url?.thumbnail(800) ?: "https://i.ytimg.com/vi/${item.key}/maxresdefault.jpg"
                        )
                    }
                }

                isLoadingFeed = false
            }
        }
    }

    LaunchedEffect(selectedLanguage) {
        val p = context.getSharedPreferences("swaara_prefs", Context.MODE_PRIVATE)
        userName = p.getString("user_name", "Music Lover") ?: "Music Lover"
        p.edit().putString("active_language", selectedLanguage).apply()
        loadLiveFeed(selectedLanguage)
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Welcome back"
        }
    }

    val displayGreeting = remember(userName, greeting) {
        val cleanName = userName.trim()
        if (cleanName.isNotBlank() && !cleanName.equals("Music Lover", ignoreCase = true)) {
            "$greeting, $cleanName"
        } else {
            greeting
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SwaaraObsidian)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 130.dp)
    ) {
        // 1. Top Header Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayGreeting,
                color = SwaaraPrimaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Search",
                        tint = SwaaraPrimaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SwaaraElevatedSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Settings",
                        tint = SwaaraPrimaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Active Live Jam Room Banner (if user is in a session)
        val activeRoom: RoomState? = currentJamRoom
        if (activeRoom != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(SwaaraViolet, SwaaraMagenta)))
                    .clickable { showJamDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.radio),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Live Jam: ${activeRoom.roomName} (${activeRoom.roomCode})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (activeRoom.isHost) "Hosting synchronized playback" else "Listening with ${activeRoom.hostName}",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Manage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Cloud Broadcast Announcement Banner
        val activeAnnouncement: SwaaraAnnouncement? = remoteConfigState.announcement
        if (activeAnnouncement != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SwaaraElevatedSurface)
                    .border(1.dp, SwaaraCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SwaaraCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sparkles),
                            contentDescription = null,
                            tint = SwaaraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeAnnouncement.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SwaaraPrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeAnnouncement.message,
                            fontSize = 11.5.sp,
                            color = SwaaraSecondaryText
                        )
                    }

                    if (activeAnnouncement.isDismissible) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Dismiss",
                            tint = SwaaraSecondaryText,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    SwaaraRemoteConfig.dismissAnnouncement(context, activeAnnouncement.id)
                                }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 2. Category / Language Discovery Rail
        RaagaLanguageChips(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { langId ->
                selectedLanguage = langId
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoadingFeed && quickGridItems.isEmpty()) {
            RaagaBrandLoader(language = selectedLanguage)
            Spacer(modifier = Modifier.height(10.dp))
            RaagaHomeFeedSkeleton()
        } else {
            // 3. Primary Content: Jump Back In (2-Column Tactile Grid)
            if (quickGridItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Jump Back In",
                        color = SwaaraPrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pick up where you left off",
                        color = SwaaraSecondaryText,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val chunked = quickGridItems.take(6).chunked(2)
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    RaagaQuickGridItem(
                                        item = item,
                                        onClick = {
                                            playSongWithInfiniteRadio(item.id, item.title, item.subtitle, item.thumbnailUrl)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Discovery: Curated Mixes & Radio (Editorial Cards)
            SwaaraCuratedShelf(
                title = "Curated Mixes",
                subtitle = "Handcrafted for your daily soundtrack",
                mixes = curatedMixes,
                onMixClick = { mix ->
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            container.searchResultRepository.searchSongs(mix.query, null)?.getOrNull()?.items
                        }
                        result?.firstOrNull()?.let { firstSong ->
                            playSongWithInfiniteRadio(
                                firstSong.key,
                                firstSong.info?.name ?: mix.title,
                                firstSong.authors?.joinToString { it.name.orEmpty() } ?: mix.title,
                                firstSong.thumbnail?.url
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Trending Music: Horizontal Ranked Cards
            if (liveTrendingSongs.isNotEmpty()) {
                val isForYou = selectedLanguage.equals("for_you", ignoreCase = true)
                val shelfTitle = if (isForYou) "Recommended For You" else "Trending ${selectedLanguage.replaceFirstChar { it.uppercase() }} Hits"
                val shelfSubtitle = if (isForYou) "Curated tracks chosen based on your listening vibe" else "Live charts updating continuously with fresh hits"
                RaagaTrendingShelf(
                    title = shelfTitle,
                    subtitle = shelfSubtitle,
                    items = liveTrendingSongs,
                    onItemClick = { item ->
                        playSongWithInfiniteRadio(item.id, item.title, item.subtitle, item.thumbnailUrl)
                    }
                )
            }

            // Instagram Reels Viral Audio Shelf
            RaagaReelsShelf(
                tracks = reelsTracks,
                isLoading = isLoadingReels,
                isResolving = isResolvingReel,
                onTrackClick = { track ->
                    coroutineScope.launch {
                        openNowPlaying()
                        InstagramTrackResolver.resolveAndPlay(
                            context = context,
                            binder = binder,
                            container = container,
                            track = track,
                            onResolvingStateChanged = { isResolvingReel = it }
                        )
                    }
                }
            )

            // A1 Swaara Community Charts Shelf (Live Crowdsourced Leaderboard)
            if (communitySongs.isNotEmpty()) {
                RaagaCommunityShelf(
                    songs = communitySongs,
                    onSongClick = { song ->
                        playSongWithInfiniteRadio(song.songId, song.title, song.artist, song.artworkUrl)
                    }
                )
            }

            // Top Artists Shelf
            if (liveArtists.isNotEmpty()) {
                val isForYou = selectedLanguage.equals("for_you", ignoreCase = true)
                val langTitle = if (isForYou) "Popular" else selectedLanguage.replaceFirstChar { it.uppercase() }
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Top $langTitle Artists",
                            color = SwaaraPrimaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to explore their full discography & albums",
                            color = SwaaraSecondaryText,
                            fontSize = 12.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(liveArtists) { artist ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        artistRoute.global(artist.id)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(SwaaraMagenta, SwaaraViolet)))
                                        .border(1.5.dp, SwaaraCyan.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artist.name.take(1),
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AsyncImage(
                                        model = artist.thumbnailUrl,
                                        contentDescription = artist.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = artist.name,
                                    color = SwaaraPrimaryText,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 6. Viral Hits Shelf
            if (viralSongs.isNotEmpty()) {
                val isForYou = selectedLanguage.equals("for_you", ignoreCase = true)
                val langTitle = if (isForYou) "Global" else selectedLanguage.replaceFirstChar { it.uppercase() }
                Spacer(modifier = Modifier.height(6.dp))
                RaagaTrendingShelf(
                    title = "Viral & Top Hits",
                    subtitle = "Most viral tracks in $langTitle",
                    items = viralSongs,
                    onItemClick = { item ->
                        playSongWithInfiniteRadio(item.id, item.title, item.subtitle, item.thumbnailUrl)
                    }
                )
            }

            // 7. Top 20 Chartbusters (Clean Ranked List Rows)
            if (topTwentyList.isNotEmpty()) {
                val isForYou = selectedLanguage.equals("for_you", ignoreCase = true)
                val langTitle = if (isForYou) "Global" else selectedLanguage.replaceFirstChar { it.uppercase() }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Top 20 Chartbusters",
                        color = SwaaraPrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Most streamed songs in $langTitle",
                        color = SwaaraSecondaryText,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    topTwentyList.forEachIndexed { idx, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SwaaraDeepSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .clickable {
                                    playSongWithInfiniteRadio(song.id, song.title, song.subtitle, song.thumbnailUrl)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number
                            Text(
                                text = (idx + 1).toString().padStart(2, '0'),
                                color = if (idx < 3) SwaaraMagenta else SwaaraSecondaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(26.dp)
                            )

                            // Artwork
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SwaaraElevatedSurface)
                            ) {
                                AsyncImage(
                                    model = song.thumbnailUrl,
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = SwaaraPrimaryText,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = song.subtitle,
                                    color = SwaaraSecondaryText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Play Button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.play),
                                    contentDescription = "Play",
                                    tint = SwaaraPrimaryText,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
