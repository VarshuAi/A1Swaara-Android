package com.varshuai.a1swaara.repositories

import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.models.Song
import app.a1swaara.core.data.enums.SongSortBy
import app.a1swaara.core.data.enums.SortOrder
import kotlinx.coroutines.flow.Flow

interface BuiltInPlaylistRepository {
    fun observeFavorites(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>>
    fun observeOffline(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>>
    fun observeTop(periodMillis: Long?, limit: Int): Flow<List<Song>>
    fun observeHistory(): Flow<List<Song>>
}

object DatabaseBuiltInPlaylistRepository : BuiltInPlaylistRepository {
    override fun observeFavorites(sortBy: SongSortBy, sortOrder: SortOrder): Flow<List<Song>> =
        Database.favorites(sortBy = sortBy, sortOrder = sortOrder)

    override fun observeOffline(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ): Flow<List<Song>> = Database.songs(sortBy = sortBy, sortOrder = sortOrder)

    override fun observeTop(periodMillis: Long?, limit: Int): Flow<List<Song>> =
        if (periodMillis == null) {
            Database.songsByPlayTimeDesc(limit = limit)
        } else {
            Database.trending(limit = limit, period = periodMillis)
        }

    override fun observeHistory(): Flow<List<Song>> = Database.history()
}
