package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Album
import com.varshuai.a1swaara.models.Song
import app.a1swaara.core.data.enums.AlbumSortBy
import app.a1swaara.core.data.enums.SortOrder
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun observeAlbums(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>>
    fun observeArtistSongs(artistId: String): Flow<List<Song>>
}

object DatabaseLibraryRepository : LibraryRepository {
    override fun observeAlbums(sortBy: AlbumSortBy, sortOrder: SortOrder): Flow<List<Album>> =
        Database.albums(sortBy = sortBy, sortOrder = sortOrder)

    override fun observeArtistSongs(artistId: String): Flow<List<Song>> =
        Database.artistSongs(artistId)
}
