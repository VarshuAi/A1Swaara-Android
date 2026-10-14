package app.a1swaara.providers.youtubemusic.innertube.models.bodies

import app.a1swaara.providers.youtubemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context = Context.DefaultWeb,
    val browseId: String,
    val params: String? = null
)
