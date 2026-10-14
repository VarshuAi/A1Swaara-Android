package com.varshuai.a1swaara.utils

import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.service.PlayerService

fun PlayerService.Binder?.playMediaItem(mediaItem: MediaItem) {
    if (this == null) return
    stopRadio()
    prefetchMediaItems(listOf(mediaItem))
    player.forcePlay(mediaItem)
}

fun PlayerService.Binder?.playSongAtIndex(
    mediaItems: List<MediaItem>,
    index: Int
) {
    if (this == null) return
    stopRadio()
    prefetchMediaItems(mediaItems)
    player.forcePlayAtIndex(mediaItems, index)
}

fun PlayerService.Binder?.shufflePlay(mediaItems: List<MediaItem>) {
    if (this == null || mediaItems.isEmpty()) return
    stopRadio()
    prefetchMediaItems(mediaItems)
    player.forcePlayFromBeginning(mediaItems.shuffled())
}

fun PlayerService.Binder?.playTrack(
    videoId: String,
    title: String,
    artist: String?,
    thumbnailUrl: String?,
    durationText: String? = null
) {
    if (this == null) return
    val cleanArtist = artist?.replace(Regex("[,&\\s]{2,}"), ", ")?.trim(',', ' ') ?: "Artist"
    val highResThumb = thumbnailUrl?.thumbnail(1200) ?: "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"
    val mediaItem = MediaItem.Builder()
        .setMediaId(videoId)
        .setUri(videoId)
        .setCustomCacheKey(videoId)
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(cleanArtist)
                .setArtworkUri(android.net.Uri.parse(highResThumb))
                .setExtras(
                    app.a1swaara.core.ui.utils.SongBundleAccessor.bundle {
                        this.artistNames = listOf(cleanArtist)
                        this.durationText = durationText
                        this.isFromPersistentQueue = false
                    }
                )
                .build()
        )
        .build()

    stopRadio()
    prefetchMediaItems(listOf(mediaItem))
    player.forcePlay(mediaItem)

    val watchEndpoint = app.a1swaara.providers.youtubemusic.innertube.models.NavigationEndpoint.Endpoint.Watch(videoId = videoId)
    setupRadio(watchEndpoint)
}
