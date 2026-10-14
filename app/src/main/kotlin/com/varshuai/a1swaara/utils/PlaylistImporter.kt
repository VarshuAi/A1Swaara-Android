package com.varshuai.a1swaara.utils

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.internal
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.SongPlaylistMap
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.BrowseBody
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.SearchBody
import app.a1swaara.providers.youtubemusic.innertube.requests.playlistPage
import app.a1swaara.providers.youtubemusic.innertube.requests.searchPage
import app.a1swaara.providers.youtubemusic.innertube.utils.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object PlaylistImporter {
    enum class Platform {
        YOUTUBE,
        SPOTIFY,
        UNKNOWN
    }

    data class ImportProgress(
        val stage: String,
        val current: Int = 0,
        val total: Int = 0,
        val currentTrack: String = ""
    )

    data class ImportResult(
        val playlistId: Long,
        val playlistName: String,
        val importedTracksCount: Int,
        val totalTracksCount: Int
    )

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun detectPlatform(input: String): Platform {
        val trimmed = input.trim()
        if (trimmed.contains("spotify.com/playlist") || trimmed.startsWith("spotify:playlist:")) {
            return Platform.SPOTIFY
        }
        if (trimmed.contains("youtube.com/playlist") ||
            trimmed.contains("youtu.be/") ||
            trimmed.contains("music.youtube.com/playlist") ||
            trimmed.startsWith("PL") ||
            trimmed.startsWith("VLPL") ||
            trimmed.startsWith("OLAK5uy_")
        ) {
            return Platform.YOUTUBE
        }
        return Platform.UNKNOWN
    }

    fun extractYouTubePlaylistId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.startsWith("PL") || trimmed.startsWith("VLPL") || trimmed.startsWith("OLAK5uy_")) {
            return trimmed
        }
        val pattern = Pattern.compile("[?&]list=([a-zA-Z0-9_-]+)")
        val matcher = pattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    fun extractSpotifyPlaylistId(input: String): String? {
        val trimmed = input.trim()
        val pattern = Pattern.compile("(?:spotify:playlist:|open\\.spotify\\.com/playlist/)([a-zA-Z0-9]+)")
        val matcher = pattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    suspend fun importPlaylist(
        inputUrl: String,
        onProgress: (ImportProgress) -> Unit = {}
    ): Result<ImportResult> = runSuspendCatching {
        when (detectPlatform(inputUrl)) {
            Platform.YOUTUBE -> {
                val playlistId = extractYouTubePlaylistId(inputUrl)
                    ?: error("Could not extract a valid YouTube playlist ID from the link")
                importYouTubePlaylist(playlistId, onProgress).getOrThrow()
            }
            Platform.SPOTIFY -> {
                val spotifyId = extractSpotifyPlaylistId(inputUrl)
                    ?: error("Could not extract a valid Spotify playlist ID from the link")
                importSpotifyPlaylist(spotifyId, onProgress).getOrThrow()
            }
            Platform.UNKNOWN -> {
                error("Unsupported link. Please paste a valid Spotify or YouTube Music playlist URL.")
            }
        }
    }

    suspend fun importYouTubePlaylist(
        playlistId: String,
        onProgress: (ImportProgress) -> Unit
    ): Result<ImportResult> = runSuspendCatching {
        onProgress(ImportProgress(stage = "Fetching YouTube playlist…"))
        val browseId = if (playlistId.startsWith("VL")) playlistId else "VL$playlistId"
        val initialPage = YoutubeMusicInnertube.playlistPage(BrowseBody(browseId = browseId))
            ?: error("Could not load playlist from YouTube")

        val fullPage = initialPage.completed().getOrThrow()
        val playlistName = fullPage.title?.takeIf { it.isNotBlank() } ?: "YouTube Playlist"
        val songs = fullPage.songsPage?.items.orEmpty()

        if (songs.isEmpty()) {
            error("Playlist contains no playable tracks")
        }

        onProgress(ImportProgress(stage = "Saving tracks to library…", current = 0, total = songs.size))

        val playlistIdInDb = withContext(Dispatchers.IO) {
            val id = Database.insert(
                Playlist(
                    name = playlistName,
                    browseId = browseId,
                    thumbnail = fullPage.thumbnail?.url
                )
            )
            if (id == -1L) error("Failed to create playlist in database")

            val mediaItems = songs.map { it.asMediaItem }
            Database.internal.runInTransaction {
                mediaItems.forEach(Database::insert)
                val mappings = mediaItems.mapIndexed { index, mediaItem ->
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = id,
                        position = index
                    )
                }
                Database.insertSongPlaylistMaps(mappings)
            }
            id
        }

        ImportResult(
            playlistId = playlistIdInDb,
            playlistName = playlistName,
            importedTracksCount = songs.size,
            totalTracksCount = songs.size
        )
    }

    suspend fun importSpotifyPlaylist(
        spotifyId: String,
        onProgress: (ImportProgress) -> Unit
    ): Result<ImportResult> = runSuspendCatching {
        onProgress(ImportProgress(stage = "Connecting to Spotify…"))

        val embedUrl = "https://open.spotify.com/embed/playlist/$spotifyId"
        val request = Request.Builder()
            .url(embedUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()

        val html = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Failed to fetch Spotify playlist (HTTP ${response.code})")
                response.body?.string() ?: error("Empty response from Spotify")
            }
        }

        val scriptPattern = Pattern.compile("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>", Pattern.DOTALL)
        val matcher = scriptPattern.matcher(html)
        if (!matcher.find()) {
            error("Could not parse Spotify playlist data. The playlist might be private.")
        }

        val jsonString = matcher.group(1) ?: error("Missing Spotify JSON data")
        val rootJson = JSONObject(jsonString)
        val entity = rootJson.optJSONObject("props")
            ?.optJSONObject("pageProps")
            ?.optJSONObject("state")
            ?.optJSONObject("data")
            ?.optJSONObject("entity") ?: error("Spotify playlist entity not found")

        val playlistName = entity.optString("name").takeIf { it.isNotBlank() } ?: "Spotify Playlist"
        val trackListArray = entity.optJSONArray("trackList") ?: error("No tracks found in playlist")
        val totalTracks = trackListArray.length()

        if (totalTracks == 0) {
            error("Spotify playlist is empty")
        }

        val matchedSongs = mutableListOf<MediaItem>()

        for (i in 0 until totalTracks) {
            currentCoroutineContext().ensureActive()
            val trackObj = trackListArray.optJSONObject(i) ?: continue
            val title = trackObj.optString("title")
            val subtitle = trackObj.optString("subtitle").replace('\u00a0', ' ').trim()
            val trackLabel = if (subtitle.isNotBlank()) "$title - $subtitle" else title

            onProgress(
                ImportProgress(
                    stage = "Matching tracks…",
                    current = i + 1,
                    total = totalTracks,
                    currentTrack = trackLabel
                )
            )

            val query = if (subtitle.isNotBlank()) "$title $subtitle" else title
            val songItem: YoutubeMusicInnertube.SongItem? = withContext(Dispatchers.IO) {
                YoutubeMusicInnertube.searchPage(
                    body = SearchBody(
                        query = query,
                        params = YoutubeMusicInnertube.SearchFilter.Song.value
                    ),
                    fromMusicShelfRendererContent = YoutubeMusicInnertube.SongItem.Companion::from
                )?.getOrNull()?.items?.firstOrNull()
            }

            if (songItem != null) {
                matchedSongs += songItem.asMediaItem
            }
        }

        if (matchedSongs.isEmpty()) {
            error("Could not match any tracks from this Spotify playlist")
        }

        val playlistIdInDb = withContext(Dispatchers.IO) {
            val id = Database.insert(Playlist(name = playlistName))
            if (id == -1L) error("Failed to create playlist in database")

            Database.internal.runInTransaction {
                matchedSongs.forEach(Database::insert)
                val mappings = matchedSongs.mapIndexed { index, mediaItem ->
                    SongPlaylistMap(
                        songId = mediaItem.mediaId,
                        playlistId = id,
                        position = index
                    )
                }
                Database.insertSongPlaylistMaps(mappings)
            }
            id
        }

        ImportResult(
            playlistId = playlistIdInDb,
            playlistName = playlistName,
            importedTracksCount = matchedSongs.size,
            totalTracksCount = totalTracks
        )
    }
}
