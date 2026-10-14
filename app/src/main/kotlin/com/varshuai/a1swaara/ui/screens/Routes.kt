package com.varshuai.a1swaara.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.handleUrl
import com.varshuai.a1swaara.models.Mood
import com.varshuai.a1swaara.preferences.DataPreferences
import com.varshuai.a1swaara.ui.screens.album.AlbumScreen
import com.varshuai.a1swaara.ui.screens.artist.ArtistScreen
import com.varshuai.a1swaara.ui.screens.pipedplaylist.PipedPlaylistScreen
import com.varshuai.a1swaara.ui.screens.playlist.PlaylistScreen
import com.varshuai.a1swaara.ui.screens.search.SearchScreen
import com.varshuai.a1swaara.ui.screens.searchresult.SearchResultScreen
import com.varshuai.a1swaara.ui.screens.settings.LogsScreen
import com.varshuai.a1swaara.ui.screens.settings.SettingsScreen
import com.varshuai.a1swaara.utils.toast
import app.a1swaara.compose.routing.Route0
import app.a1swaara.compose.routing.Route1
import app.a1swaara.compose.routing.Route3
import app.a1swaara.compose.routing.Route4
import app.a1swaara.compose.routing.RouteHandlerScope
import app.a1swaara.core.data.enums.BuiltInPlaylist
import io.ktor.http.Url
import java.util.UUID

/**
 * Marker class for linters that a composable is a route and should not be handled like a regular
 * composable, but rather as an entrypoint.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Route

val albumRoute = Route1<String>("albumRoute")
val artistRoute = Route1<String>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val localPlaylistRoute = Route1<Long>("localPlaylistRoute")
val logsRoute = Route0("logsRoute")
val pipedPlaylistRoute = Route3<String, String, String>("pipedPlaylistRoute")
val playlistRoute = Route4<String, String?, Int?, Boolean>("playlistRoute")
val moodRoute = Route1<Mood>("moodRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@Composable
fun RouteHandlerScope.GlobalRoutes() {
    val context = LocalContext.current
    val errorMessage = stringResource(R.string.error_message)
    val errorUrlPattern = stringResource(R.string.error_url, "%s")
    val binder = LocalPlayerServiceBinder.current
    val onlineSearchRepository = LocalAppContainer.current.onlineSearchRepository

    albumRoute { browseId ->
        AlbumScreen(browseId = browseId)
    }

    artistRoute { browseId ->
        ArtistScreen(browseId = browseId)
    }

    logsRoute {
        LogsScreen()
    }

    pipedPlaylistRoute { apiBaseUrl, sessionToken, playlistId ->
        val safeApiBaseUrl = runCatching { Url(apiBaseUrl) }.getOrNull()
        if (safeApiBaseUrl == null) {
            context.toast(errorUrlPattern.format(apiBaseUrl))
            return@pipedPlaylistRoute
        }
        val safePlaylistId = runCatching { UUID.fromString(playlistId) }.getOrNull()
        if (safePlaylistId == null) {
            context.toast(errorMessage)
            return@pipedPlaylistRoute
        }
        PipedPlaylistScreen(
            apiBaseUrl = safeApiBaseUrl,
            sessionToken = sessionToken,
            playlistId = safePlaylistId
        )
    }

    playlistRoute { browseId, params, maxDepth, shouldDedup ->
        PlaylistScreen(
            browseId = browseId,
            params = params,
            maxDepth = maxDepth,
            shouldDedup = shouldDedup
        )
    }

    settingsRoute {
        SettingsScreen()
    }

    searchRoute { initialTextInput ->
        SearchScreen(
            initialTextInput = initialTextInput,
            onSearch = { rawQuery ->
                val query = rawQuery.trim()
                if (query.isNotEmpty()) {
                    if (!DataPreferences.pauseSearchHistory) onlineSearchRepository.saveHistory(query)

                    args[0] = query
                    if (child == searchResultRoute) replace(null)
                    replace(searchResultRoute)
                }
            },
            onViewPlaylist = { url ->
                with(context) {
                    runCatching {
                        handleUrl(url.toUri(), binder)
                    }.onFailure {
                        toast(getString(R.string.error_url, url))
                    }
                }
            }
        )
    }

    searchResultRoute { query ->
        SearchResultScreen(
            query = query,
            onSearchAgain = { searchRoute(query) }
        )
    }
}
