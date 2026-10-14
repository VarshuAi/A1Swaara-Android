package com.varshuai.a1swaara.repositories

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.Database
import com.varshuai.a1swaara.transaction

interface PrecacheRepository {
    fun isFavorite(mediaId: String): Boolean
    fun insertMediaItem(mediaItem: MediaItem): Result<Unit>
}

object DatabasePrecacheRepository : PrecacheRepository {
    override fun isFavorite(mediaId: String): Boolean =
        runCatching { Database.likedAtNow(mediaId) != null }.getOrDefault(false)

    override fun insertMediaItem(mediaItem: MediaItem): Result<Unit> = runCatching {
        transaction { Database.insert(mediaItem) }
    }.map { Unit }
}
