package com.varshuai.a1swaara.repositories

import app.a1swaara.providers.youtubemusic.innertube.YoutubeMusicInnertube
import app.a1swaara.providers.youtubemusic.innertube.models.bodies.BrowseBody
import app.a1swaara.providers.youtubemusic.innertube.requests.BrowseResult
import app.a1swaara.providers.youtubemusic.innertube.requests.browse

interface MoodRepository {
    suspend fun fetchMoodPage(browseId: String, params: String?): Result<BrowseResult>?
}

object YoutubeMusicInnertubeMoodRepository : MoodRepository {
    override suspend fun fetchMoodPage(browseId: String, params: String?): Result<BrowseResult>? =
        YoutubeMusicInnertube.browse(BrowseBody(browseId = browseId, params = params))
}
