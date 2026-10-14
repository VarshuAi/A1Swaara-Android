package com.varshuai.a1swaara.providers.instagram

/**
 * Data representation of a track extracted from Instagram Reels audio and music sticker queries.
 */
data class InstagramTrack(
    val id: String,
    val title: String,
    val displayArtist: String,
    val durationMs: Long = 0L,
    val coverArtworkUri: String? = null,
    val previewUrl: String? = null,
    val reelsCountFormatted: String? = null,
    val isTrending: Boolean = false,
    val audioClusterId: String? = null
)
