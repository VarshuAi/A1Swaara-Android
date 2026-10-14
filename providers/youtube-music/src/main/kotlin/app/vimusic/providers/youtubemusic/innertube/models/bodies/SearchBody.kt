package app.a1swaara.providers.youtubemusic.innertube.models.bodies

import app.a1swaara.providers.youtubemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context = Context.DefaultWeb,
    val query: String,
    val params: String
)
