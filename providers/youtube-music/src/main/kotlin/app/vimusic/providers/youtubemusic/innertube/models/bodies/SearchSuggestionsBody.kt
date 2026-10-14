package app.a1swaara.providers.youtubemusic.innertube.models.bodies

import app.a1swaara.providers.youtubemusic.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsBody(
    val context: Context = Context.DefaultWeb,
    val input: String
)
