package com.varshuai.a1swaara.ui.modifiers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MediaItem
import com.varshuai.a1swaara.R
import com.varshuai.a1swaara.LocalAppContainer
import com.varshuai.a1swaara.LocalPlayerServiceBinder
import com.varshuai.a1swaara.models.Song
import com.varshuai.a1swaara.preferences.AppearancePreferences
import com.varshuai.a1swaara.service.isLocal
import com.varshuai.a1swaara.utils.addNext
import com.varshuai.a1swaara.utils.removeSongFromQueue

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun Modifier.songSwipeActions(
    key: Any,
    mediaItem: MediaItem,
    songToHide: Song? = null,
    requireUnconsumed: Boolean = true,
    onSwipeLeftRequested: ((Song) -> Unit)? = null,
    onHideSong: ((Song) -> Unit)? = null
): Modifier {
    val binder = LocalPlayerServiceBinder.current
    val defaultOnHideSong = onHideSong ?: LocalAppContainer.current.songsRepository::deleteSong
    val canSwipeLeft = AppearancePreferences.swipeToHideSong &&
        songToHide != null &&
        onSwipeLeftRequested != null

    if (!canSwipeLeft && !AppearancePreferences.swipeRightToPlayNext) {
        return this
    }

    return this.swipeToAction(
        key = key,
        requireUnconsumed = requireUnconsumed,
        enableSwipeLeft = canSwipeLeft,
        enableSwipeRight = AppearancePreferences.swipeRightToPlayNext,
        leftActionIcon = if (canSwipeLeft) R.drawable.trash else null,
        rightActionIcon = if (AppearancePreferences.swipeRightToPlayNext) {
            R.drawable.play_skip_forward
        } else {
            null
        },
        onSwipeLeft = { animationJob ->
            if (canSwipeLeft) {
                val song = requireNotNull(songToHide)
                if (AppearancePreferences.swipeToHideSongConfirm) {
                    requireNotNull(onSwipeLeftRequested).invoke(song)
                } else {
                    binder?.player?.removeSongFromQueue(song.id)
                    if (!song.isLocal) binder?.cache?.removeResource(song.id)
                    defaultOnHideSong(song)
                }
            }
            animationJob.join()
        },
        onSwipeRight = { animationJob ->
            binder?.player?.addNext(mediaItem)
            animationJob.join()
        }
    )
}
