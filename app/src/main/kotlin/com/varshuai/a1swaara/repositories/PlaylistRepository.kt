package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Playlist
import com.varshuai.a1swaara.models.SongPlaylistMap
import com.varshuai.a1swaara.query
import com.varshuai.a1swaara.transaction
import com.varshuai.a1swaara.utils.asMediaItem
import com.varshuai.a1swaara.utils.completed
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.BrowseBody
import app.a1swaara.providers.youtubemusic.innertube.requests.playlistPage

interface PlaylistRepository {
    suspend fun fetchPlaylistPage(
        browseId: String,
        params: String?,
        maxDepth: Int?,
        shouldDedup: Boolean
    ): YoutubeMusicInnertube.PlaylistOrAlbumPage?

    fun importPlaylist(
        name: String,
        browseId: String,
        thumbnailUrl: String?,
        songs: List<YoutubeMusicInnertube.SongItem>?
    )
}

object DatabasePlaylistRepository : PlaylistRepository {
    override suspend fun fetchPlaylistPage(
        browseId: String,
        params: String?,
        maxDepth: Int?,
        shouldDedup: Boolean
    ): YoutubeMusicInnertube.PlaylistOrAlbumPage? =
        YoutubeMusicInnertube
            .playlistPage(BrowseBody(browseId = browseId, params = params))
            ?.completed(maxDepth = maxDepth ?: Int.MAX_VALUE, shouldDedup = shouldDedup)
            ?.getOrNull()

    override fun importPlaylist(
        name: String,
        browseId: String,
        thumbnailUrl: String?,
        songs: List<YoutubeMusicInnertube.SongItem>?
    ) {
        query {
            transaction {
                val playlistId = Database.insert(
                    Playlist(
                        name = name,
                        browseId = browseId,
                        thumbnail = thumbnailUrl
                    )
                )

                songs
                    ?.map(YoutubeMusicInnertube.SongItem::asMediaItem)
                    ?.onEach(Database::insert)
                    ?.mapIndexed { index, mediaItem ->
                        SongPlaylistMap(
                            songId = mediaItem.mediaId,
                            playlistId = playlistId,
                            position = index
                        )
                    }
                    ?.let(Database::insertSongPlaylistMaps)
            }
        }
    }
}
