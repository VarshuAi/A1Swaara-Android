package com.varshuai.a1swaara.providers.instagram

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Client for interacting with Instagram Web GraphQL Music & Reels Audio endpoints.
 */
object InstagramMusicClient {
    private const val TAG = "InstagramMusicClient"
    private const val GRAPHQL_URL = "https://www.instagram.com/api/graphql"
    private const val IG_APP_ID = "936619743392459"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private const val DOC_ID_PLAYLIST = "27812422605032626"
    private const val DOC_ID_SEARCH = "27152225807728809"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private var cachedTrendingTracks: List<InstagramTrack>? = null

    /**
     * Fetches the official Trending Reels Music playlist.
     */
    suspend fun getTrendingReelsPlaylist(count: Int = 15): List<InstagramTrack> = withContext(Dispatchers.IO) {
        cachedTrendingTracks?.takeIf { it.isNotEmpty() }?.let {
            return@withContext it
        }

        try {
            val variablesJson = JSONObject().apply {
                put("count", count)
            }.toString()

            val formBody = FormBody.Builder()
                .add("doc_id", DOC_ID_PLAYLIST)
                .add("variables", variablesJson)
                .add("fb_api_req_friendly_name", "playlistMusicRefetchQuery")
                .build()

            val request = Request.Builder()
                .url(GRAPHQL_URL)
                .post(formBody)
                .header("X-IG-App-ID", IG_APP_ID)
                .header("User-Agent", USER_AGENT)
                .header("Origin", "https://www.instagram.com")
                .header("Referer", "https://www.instagram.com/")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val tracks = parsePlaylistResponse(bodyString)
                        if (tracks.isNotEmpty()) {
                            cachedTrendingTracks = tracks
                            return@withContext tracks
                        }
                    }
                } else {
                    Log.w(TAG, "Trending Reels query failed with HTTP code ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trending Reels playlist", e)
        }

        // Return curated viral reels catalog as fallback
        val fallback = getFallbackReelsTracks()
        cachedTrendingTracks = fallback
        fallback
    }

    /**
     * Searches Instagram audio catalog using the Music Sticker search endpoint.
     */
    suspend fun searchMusic(query: String): List<InstagramTrack> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        try {
            val paramsObj = JSONObject().apply {
                put("product", "IG_DIRECT_MUSIC_STICKER")
                put("q", query)
            }
            val variablesJson = JSONObject().apply {
                put("params", paramsObj)
            }.toString()

            val formBody = FormBody.Builder()
                .add("doc_id", DOC_ID_SEARCH)
                .add("variables", variablesJson)
                .add("fb_api_req_friendly_name", "IGDComposerRichContentMusicSearchQuery")
                .build()

            val request = Request.Builder()
                .url(GRAPHQL_URL)
                .post(formBody)
                .header("X-IG-App-ID", IG_APP_ID)
                .header("User-Agent", USER_AGENT)
                .header("Origin", "https://www.instagram.com")
                .header("Referer", "https://www.instagram.com/")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        return@withContext parseSearchResponse(bodyString)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Instagram audio catalog", e)
        }

        emptyList()
    }

    private fun parsePlaylistResponse(jsonString: String): List<InstagramTrack> {
        val tracks = mutableListOf<InstagramTrack>()
        try {
            val root = JSONObject(jsonString)
            val data = root.optJSONObject("data") ?: return tracks
            val connection = data.optJSONObject("xfb_ig_audio_playlist_connection") ?: return tracks
            val items = connection.optJSONObject("items") ?: return tracks
            val edges = items.optJSONArray("edges") ?: return tracks

            for (i in 0 until edges.length()) {
                val edge = edges.optJSONObject(i) ?: continue
                val node = edge.optJSONObject("node") ?: continue
                val trackObj = node.optJSONObject("track") ?: continue
                val metadata = node.optJSONObject("metadata")

                val track = parseTrackObject(trackObj, metadata)
                if (track != null) {
                    tracks.add(track)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing playlist JSON response", e)
        }
        return tracks
    }

    private fun parseSearchResponse(jsonString: String): List<InstagramTrack> {
        val tracks = mutableListOf<InstagramTrack>()
        try {
            val root = JSONObject(jsonString)
            val data = root.optJSONObject("data") ?: return tracks
            val searchObj = data.optJSONObject("xfb_ig_audio_search") ?: return tracks
            val items = searchObj.optJSONArray("items") ?: return tracks

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val trackObj = item.optJSONObject("track") ?: continue
                val metadata = item.optJSONObject("metadata")

                val track = parseTrackObject(trackObj, metadata)
                if (track != null) {
                    tracks.add(track)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing search JSON response", e)
        }
        return tracks
    }

    private fun parseTrackObject(trackObj: JSONObject, metadata: JSONObject?): InstagramTrack? {
        val id = trackObj.optString("id")
        val title = trackObj.optString("title")
        val artist = trackObj.optString("display_artist")

        if (id.isBlank() || title.isBlank()) return null

        val durationMs = trackObj.optLong("duration_in_ms", 0L)
        val coverArtwork = trackObj.optString("cover_artwork_uri").takeIf { it.isNotBlank() }
        val previewUrl = trackObj.optString("web_30s_preview_download_url").takeIf { it.isNotBlank() }
        val clusterId = trackObj.optString("audio_cluster_id").takeIf { it.isNotBlank() }

        val reelsCount = metadata?.optString("formatted_clips_media_count")?.takeIf { it.isNotBlank() }
        val isTrending = metadata?.optBoolean("is_trending_in_clips", false) ?: false

        return InstagramTrack(
            id = id,
            title = title,
            displayArtist = if (artist.isNotBlank()) artist else "Various Artists",
            durationMs = durationMs,
            coverArtworkUri = coverArtwork,
            previewUrl = previewUrl,
            reelsCountFormatted = reelsCount,
            isTrending = isTrending,
            audioClusterId = clusterId
        )
    }

    private fun getFallbackReelsTracks(): List<InstagramTrack> {
        return listOf(
            InstagramTrack(
                id = "2164387884446896",
                title = "Melwa Ghume Aaju Ratiya Me Aawen Ge",
                displayArtist = "Kallu Bihari Yadav, Srishti Bharti",
                durationMs = 264000L,
                reelsCountFormatted = "12K reels",
                isTrending = true
            ),
            InstagramTrack(
                id = "907270093614688",
                title = "Darshana",
                displayArtist = "Hesham Abdul Wahab, Darshana Rajendran",
                durationMs = 210000L,
                reelsCountFormatted = "480K reels",
                isTrending = true
            ),
            InstagramTrack(
                id = "1357882629288957",
                title = "Das and Parvathy (Instrumental)",
                displayArtist = "Anirudh Ravichander",
                durationMs = 145000L,
                reelsCountFormatted = "125K reels",
                isTrending = true
            ),
            InstagramTrack(
                id = "596347273202999",
                title = "Domination",
                displayArtist = "graur",
                durationMs = 120000L,
                reelsCountFormatted = "65K reels",
                isTrending = true
            ),
            InstagramTrack(
                id = "10073085949403100",
                title = "Theme Music",
                displayArtist = "Arjun Janya, V.Nagendra Prasad",
                durationMs = 180000L,
                reelsCountFormatted = "85K reels",
                isTrending = true
            ),
            InstagramTrack(
                id = "4366450180349450",
                title = "Drishyam 3 - Motion Poster Theme",
                displayArtist = "Anil Johnson",
                durationMs = 95000L,
                reelsCountFormatted = "34K reels",
                isTrending = true
            )
        )
    }
}
