package com.varshuai.a1swaara.repositories

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.utils.asMediaItem
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.BrowseBody
import app.a1swaara.providers.youtubemusic.innertube.requests.playlistPage
import app.a1swaara.providers.youtubemusic.innertube.requests.song

interface DeepLinkRepository {
    suspend fun resolveAlbumBrowseIdFromPlaylist(browseId: String): String?
    suspend fun resolveSongMediaItem(videoId: String): MediaItem?
}

object YoutubeMusicInnertubeDeepLinkRepository : DeepLinkRepository {
    override suspend fun resolveAlbumBrowseIdFromPlaylist(browseId: String): String? =
        YoutubeMusicInnertube.playlistPage(body = BrowseBody(browseId = browseId))
            ?.getOrNull()
            ?.songsPage
            ?.items
            ?.firstOrNull()
            ?.album
            ?.endpoint
            ?.browseId

    override suspend fun resolveSongMediaItem(videoId: String): MediaItem? =
        YoutubeMusicInnertube.song(videoId)?.getOrNull()?.asMediaItem
}
