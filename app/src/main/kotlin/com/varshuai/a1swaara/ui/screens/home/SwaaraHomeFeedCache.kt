package com.varshuai.a1swaara.ui.screens.home

import com.varshuai.a1swaara.cloud.TrendingSong
import com.varshuai.a1swaara.ui.components.SwaaraDisplayItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory feed cache for SwaaraHomeScreen.
 * Preserves the loaded home feed when navigating away to Settings, Search, Library, or Player,
 * ensuring the home feed does NOT reload when returning to Home, unless the user pulls down to refresh
 * or actively switches language categories.
 */
object SwaaraHomeFeedCache {
    var cachedLanguage: String? = null
    var trendingSongs: List<SwaaraDisplayItem> = emptyList()
    var quickGridItems: List<SwaaraDisplayItem> = emptyList()
    var liveArtists: List<SwaaraArtistDisplayItem> = emptyList()
    var viralSongs: List<SwaaraDisplayItem> = emptyList()
    var topTwentyList: List<SwaaraDisplayItem> = emptyList()
    var communitySongs: List<TrendingSong> = emptyList()
    var isInitialized: Boolean = false

    fun hasDataFor(language: String): Boolean {
        return isInitialized && cachedLanguage.equals(language, ignoreCase = true) && trendingSongs.isNotEmpty()
    }

    fun clear() {
        cachedLanguage = null
        trendingSongs = emptyList()
        quickGridItems = emptyList()
        liveArtists = emptyList()
        viralSongs = emptyList()
        topTwentyList = emptyList()
        communitySongs = emptyList()
        isInitialized = false
    }
}
