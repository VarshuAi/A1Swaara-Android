package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Album
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.query
import com.varshuai.a1swaara.models.SongAlbumMap
import com.varshuai.a1swaara.transaction
import com.varshuai.a1swaara.utils.asMediaItem
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.BrowseBody
import app.a1swaara.providers.youtubemusic.innertube.requests.albumPage
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbum(browseId: String): Flow<Album?>
    fun observeAlbumSongs(browseId: String): Flow<List<Song>>
    suspend fun fetchAlbumPage(browseId: String): Result<YoutubeMusicInnertube.PlaylistOrAlbumPage?>?
    fun replaceAlbumFromPage(
        browseId: String,
        bookmarkedAt: Long?,
        page: YoutubeMusicInnertube.PlaylistOrAlbumPage
    )

    fun updateAlbum(album: Album)
}

object DatabaseAlbumRepository : AlbumRepository {
    override fun observeAlbum(browseId: String): Flow<Album?> = Database.album(browseId)

    override fun observeAlbumSongs(browseId: String): Flow<List<Song>> = Database.albumSongs(browseId)

    override suspend fun fetchAlbumPage(browseId: String): Result<YoutubeMusicInnertube.PlaylistOrAlbumPage?>? =
        YoutubeMusicInnertube.albumPage(BrowseBody(browseId = browseId))

    override fun replaceAlbumFromPage(
        browseId: String,
        bookmarkedAt: Long?,
        page: YoutubeMusicInnertube.PlaylistOrAlbumPage
    ) {
        val songAlbumMaps = page
            .songsPage
            ?.items
            ?.map { it.asMediaItem }
            ?.onEach(Database::insert)
            ?.mapIndexed { position, mediaItem ->
                SongAlbumMap(
                    songId = mediaItem.mediaId,
                    albumId = browseId,
                    position = position
                )
            }
            ?: emptyList()

        transaction {
            Database.clearAlbum(browseId)
            Database.upsert(
                album = Album(
                    id = browseId,
                    title = page.title,
                    description = page.description,
                    thumbnailUrl = page.thumbnail?.url,
                    year = page.year,
                    authorsText = page.authors?.joinToString("") { it.name.orEmpty() },
                    shareUrl = page.url,
                    timestamp = System.currentTimeMillis(),
                    bookmarkedAt = bookmarkedAt,
                    otherInfo = page.otherInfo
                ),
                songAlbumMaps = songAlbumMaps
            )
        }
    }

    override fun updateAlbum(album: Album) {
        query { Database.update(album) }
    }
}
