package app.a1swaara.providers.youtubemusic.innertube.requests

import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.SearchSuggestionsResponse
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.SearchSuggestionsBody
import app.a1swaara.providers.utils.runCatchingCancellable

suspend fun YoutubeMusicInnertube.searchSuggestions(body: SearchSuggestionsBody) = runCatchingCancellable {
    val response = client.post<SearchSuggestionsResponse>(
        SEARCH_SUGGESTIONS, body,
        fieldMask = "contents.searchSuggestionsSectionRenderer.contents.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query",
    )

    response
        .contents
        ?.firstOrNull()
        ?.searchSuggestionsSectionRenderer
        ?.contents
        ?.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        }
}
