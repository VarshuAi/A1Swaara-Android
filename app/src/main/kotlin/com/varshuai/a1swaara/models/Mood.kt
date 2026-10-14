package com.varshuai.a1swaara.models

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import app.a1swaara.core.ui.ColorParceler
import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith

@Parcelize
data class Mood(
    val name: String,
    val color: @WriteWith<ColorParceler> Color,
    val browseId: String?,
    val params: String?
) : Parcelable

fun YoutubeMusicInnertube.Mood.Item.toUiMood() = Mood(
    name = title,
    color = Color(stripeColor),
    browseId = endpoint.browseId,
    params = endpoint.params
)
