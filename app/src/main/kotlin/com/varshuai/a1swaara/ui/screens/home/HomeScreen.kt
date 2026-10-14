package com.varshuai.a1swaara.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.models.toUiMood
import com.varshuai.a1swaara.preferences.UIStatePreferences
import com.varshuai.a1swaara.ui.components.A1SwaraGlassDock
import com.varshuai.a1swaara.ui.screens.GlobalRoutes
import com.varshuai.a1swaara.ui.screens.Route
import com.varshuai.a1swaara.ui.screens.albumRoute
import com.varshuai.a1swaara.ui.screens.artistRoute
import com.varshuai.a1swaara.ui.screens.builtInPlaylistRoute
import com.varshuai.a1swaara.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.varshuai.a1swaara.ui.screens.library.A1SwaraLibraryScreen
import com.varshuai.a1swaara.ui.screens.localPlaylistRoute
import com.varshuai.a1swaara.ui.screens.localplaylist.LocalPlaylistScreen
import com.varshuai.a1swaara.ui.screens.mood.MoodScreen
import com.varshuai.a1swaara.ui.screens.mood.MoreAlbumsScreen
import com.varshuai.a1swaara.ui.screens.mood.MoreMoodsScreen
import com.varshuai.a1swaara.ui.screens.moodRoute
import com.varshuai.a1swaara.ui.screens.pipedPlaylistRoute
import com.varshuai.a1swaara.ui.screens.playlistRoute
import com.varshuai.a1swaara.ui.screens.search.A1SwaraSearchScreen
import com.varshuai.a1swaara.ui.screens.searchRoute
import com.varshuai.a1swaara.ui.screens.settingsRoute
import app.a1swaara.compose.persist.PersistMapCleanup
import app.a1swaara.compose.routing.Route0
import app.a1swaara.compose.routing.RouteHandler

private val moreMoodsRoute = Route0("moreMoodsRoute")
private val moreAlbumsRoute = Route0("moreAlbumsRoute")

@Route
@Composable
fun HomeScreen() {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler {
        GlobalRoutes()

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(playlistId = playlistId)
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(builtInPlaylist = builtInPlaylist)
        }

        moodRoute { mood ->
            MoodScreen(mood = mood)
        }

        moreMoodsRoute {
            MoreMoodsScreen()
        }

        moreAlbumsRoute {
            MoreAlbumsScreen()
        }

        Content {
            var currentTab by rememberSaveable { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0C))
            ) {
                saveableStateHolder.SaveableStateProvider(key = currentTab) {
                    when (currentTab) {
                        0 -> A1SwaraHomeScreen(
                            onSearchClick = { searchRoute("") },
                            onSettingsClick = { settingsRoute() }
                        )

                        1 -> A1SwaraSearchScreen(
                            onSearchSubmit = { searchRoute(it) }
                        )

                        2 -> A1SwaraLibraryScreen()
                    }
                }

                A1SwaraGlassDock(
                    currentTabIndex = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
