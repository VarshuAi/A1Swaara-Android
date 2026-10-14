package app.a1swaara.providers.youtubemusic.innertube.models.bodies

import app.a1swaara.providers.youtubemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class ContinuationBody(
    val context: Context = Context.DefaultWeb,
    val continuation: String
)
