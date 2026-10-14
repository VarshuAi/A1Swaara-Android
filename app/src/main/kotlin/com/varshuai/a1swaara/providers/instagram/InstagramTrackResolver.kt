package com.varshuai.a1swaara.providers.instagram

import android.content.Context
import android.util.Log
import com.varshuai.a1swaara.AppContainer
import com.varshuai.a1swaara.service.PlayerService
import com.varshuai.a1swaara.utils.playTrack
import com.varshuai.a1swaara.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves an Instagram track to a full-length, high-fidelity YouTube Music stream for ExoPlayer.
 */
object InstagramTrackResolver {
    private const val TAG = "InstagramTrackResolver"

    /**
     * Resolves the Instagram track to its full-length streaming stream and plays it.
     */
    suspend fun resolveAndPlay(
        context: Context,
        binder: PlayerService.Binder?,
        container: AppContainer,
        track: InstagramTrack,
        onResolvingStateChanged: ((Boolean) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        if (binder == null) return@withContext

        try {
            onResolvingStateChanged?.invoke(true)
            val query = "${track.title} ${track.displayArtist}".trim()
            Log.d(TAG, "Resolving full song for Instagram track: $query")

            val searchResult = container.searchResultRepository.searchSongs(query, null)?.getOrNull()
            val bestMatch = searchResult?.items?.firstOrNull()

            if (bestMatch != null) {
                withContext(Dispatchers.Main) {
                    onResolvingStateChanged?.invoke(false)
                    binder.playTrack(
                        videoId = bestMatch.key,
                        title = bestMatch.info?.name ?: track.title,
                        artist = bestMatch.authors?.joinToString(", ") { it.name.orEmpty() } ?: track.displayArtist,
                        thumbnailUrl = bestMatch.thumbnail?.url ?: track.coverArtworkUri
                    )
                }
                return@withContext
            }

            // Fallback search with title only if artist combo failed
            val fallbackResult = container.searchResultRepository.searchSongs(track.title, null)?.getOrNull()
            val fallbackMatch = fallbackResult?.items?.firstOrNull()

            if (fallbackMatch != null) {
                withContext(Dispatchers.Main) {
                    onResolvingStateChanged?.invoke(false)
                    binder.playTrack(
                        videoId = fallbackMatch.key,
                        title = fallbackMatch.info?.name ?: track.title,
                        artist = fallbackMatch.authors?.joinToString(", ") { it.name.orEmpty() } ?: track.displayArtist,
                        thumbnailUrl = fallbackMatch.thumbnail?.url ?: track.coverArtworkUri
                    )
                }
                return@withContext
            }

            withContext(Dispatchers.Main) {
                onResolvingStateChanged?.invoke(false)
                context.toast("Unable to find full audio stream for this viral sound")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving full track", e)
            withContext(Dispatchers.Main) {
                onResolvingStateChanged?.invoke(false)
                context.toast("Playback error: ${e.message}")
            }
        }
    }
}
